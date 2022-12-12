package com.badasstechie.infinitychat.Chat;

import com.badasstechie.infinitychat.AppUser.AppUser;
import com.badasstechie.infinitychat.Auth.AuthService;
import com.badasstechie.infinitychat.Message.MessageRepository;
import com.badasstechie.infinitychat.Utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final AuthService authService;

    @Autowired
    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository, AuthService authService) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.authService = authService;
    }

    public ChatResponse mapChatToResponse(Chat chat, AppUser you) {
        return new ChatResponse(
                chat.getChatId(),
                chat.otherMember(you).getUsername(),
                new String(chat.otherMember(you).getAvatar()),
                getLastMessage(chat, you),
                Time.formatTime(chat.getModified()),
                messageRepository.countByChat(chat)
        );
    }

    @Transactional(readOnly = true)
    public ChatResponse getChat(Long id) {
        Optional<Chat> chatOptional = chatRepository.findById(id);
        if (chatOptional.isEmpty())
            throw new IllegalStateException("Chat with id " + id + " not found");
        Chat chat = chatOptional.get();

        // check if user is a participant
        AppUser you = authService.getUserFromAuthentication();
        if (!chat.isMember(you))
            throw new IllegalStateException("You are not a member of this chat");

        return mapChatToResponse(chat, you);
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getYourChats() {
        AppUser you = authService.getUserFromAuthentication();
        return chatRepository
                .findAllByOrderByModifiedDesc()
                .stream()
                .filter(chat -> chat.isMember(you))
                .map(chat -> mapChatToResponse(chat, you))
                .toList();
    }

    private String getLastMessage(Chat chat, AppUser you) {
        return messageRepository
                .findTopByChatOrderByCreatedDesc(chat)
                .map(message -> message.getSender().equals(you) ? "You: " + message.getText() : message.getText())
                .orElse("No messages");
    }

    @Transactional
    public ResponseEntity<String> deleteChat(Long id) {
        Optional<Chat> chatOptional = chatRepository.findById(id);
        if (chatOptional.isEmpty())
            return new ResponseEntity<>("Chat of id " + id + " not found", HttpStatus.NOT_FOUND);
        Chat chat = chatOptional.get();

        // check if user is a participant
        if (!chat.isMember(authService.getUserFromAuthentication()))
            return new ResponseEntity<>("You are not a member of this chat", HttpStatus.FORBIDDEN);

        // delete all messages in this chat
        messageRepository.deleteAllByChat_ChatId(id);

        chatRepository.delete(chatOptional.get());  // delete chat
        return new ResponseEntity<>("Chat deleted", HttpStatus.OK);
    }

    public void deleteAllChatsByUser(AppUser user) {
        List<Chat> chats = chatRepository.findAllByCreator(user);
        chats.addAll(chatRepository.findAllByMember(user));
        for (Chat chat : chats)
            deleteChat(chat.getChatId());
    }
}
