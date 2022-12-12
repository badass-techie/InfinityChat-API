package com.badasstechie.infinitychat.Message;

import com.badasstechie.infinitychat.AppUser.AppUser;
import com.badasstechie.infinitychat.AppUser.AppUserRepository;
import com.badasstechie.infinitychat.Auth.AuthService;
import com.badasstechie.infinitychat.Chat.Chat;
import com.badasstechie.infinitychat.Chat.ChatRepository;
import com.badasstechie.infinitychat.Chat.ChatService;
import com.badasstechie.infinitychat.Utils.Pair;
import com.badasstechie.infinitychat.Utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final AppUserRepository appUserRepository;
    private final AuthService authService;
    private final ChatService chatService;

    @Autowired
    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository, AppUserRepository appUserRepository, ChatService chatService, AuthService authService) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.appUserRepository = appUserRepository;
        this.authService = authService;
        this.chatService = chatService;
    }

    @Transactional
    public Pair<MessageWebsocketResponse, Boolean> sendMessage(String recipientName, String text) {
        // get sender
        AppUser sender = authService.getUserFromAuthentication();

        // get recipient
        Optional<AppUser> recipientOptional = appUserRepository.findByUsername(recipientName);
        if (recipientOptional.isEmpty())
            throw new RuntimeException("Recipient " + recipientName + " not found");
        AppUser recipient = recipientOptional.get();

        // check if sender and recipient are the same
        if (sender.equals(recipient))
            throw new RuntimeException("You cannot message yourself");

        // get chat
        Optional<Chat> chatOptional = chatRepository.findByCreatorAndMember(sender, recipient)
                .or(() -> chatRepository.findByCreatorAndMember(recipient, sender));
        Chat chat;
        boolean newChatCreated = false;
        if(chatOptional.isEmpty()) {    // if chat doesn't exist
            chat = Chat.builder()
                    .creator(sender)
                    .member(recipient)
                    .created(Instant.now())
                    .modified(Instant.now())
                    .build();   // create chat with sender and recipient
            chatRepository.save(chat);
            newChatCreated = true;
        } else {
            chat = chatOptional.get();
        }

        // update modified time of chat
        chat.setModified(Instant.now());
        chatRepository.save(chat);

        // create message
        Message message = Message.builder()
                .text(text)
                .chat(chat)
                .sender(sender)
                .created(Instant.now())
                .build();
        messageRepository.save(message);

        return new Pair<>(mapMessageToWebsocketResponse(message, chat, sender, recipient), newChatCreated);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByChat(Long chatId){
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        if (chatOptional.isEmpty())
            throw new RuntimeException("Chat of id " + chatId + " not found");

        return messageRepository
                .findAllByChat_ChatIdOrderByCreated(chatId)
                .stream()
                .map(this::mapMessageToResponse)
                .toList();
    }

    private MessageResponse mapMessageToResponse(Message message) {
        return new MessageResponse(
            message.getMessageId(),
            message.getText(),
            message.getSender().getUsername(),
            Time.formatTime(message.getCreated())
        );
    }

    private MessageWebsocketResponse mapMessageToWebsocketResponse(Message message, Chat chat, AppUser sender, AppUser recipient) {
        return new MessageWebsocketResponse(
                mapMessageToResponse(message),
                chatService.mapChatToResponse(chat, sender),
                chatService.mapChatToResponse(chat, recipient)
        );
    }
}
