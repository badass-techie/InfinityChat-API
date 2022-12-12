package com.badasstechie.infinitychat.Message;

import com.badasstechie.infinitychat.Utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@PreAuthorize("hasAuthority('SCOPE_USER')")
public class MessageController {
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageController(MessageService messageService, SimpMessagingTemplate simpMessagingTemplate) {
        this.messageService = messageService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    //create
    @PostMapping("/to/{recipientName}")
    public void sendMessage(@PathVariable String recipientName, @RequestBody String text) {
        Pair<MessageWebsocketResponse, Boolean> message = messageService.sendMessage(recipientName, text);
        MessageWebsocketResponse payload = message.first;
        boolean newChatCreated = message.second;

        String senderName = payload.message().senderName();

        if (newChatCreated) {
            // we need a separate topic for new chats
            // otherwise on the client we would only get messages from existing chats
            simpMessagingTemplate.convertAndSend("/topic/chat/" + senderName, payload);
            simpMessagingTemplate.convertAndSend("/topic/chat/" + recipientName, payload);
        }
        else {
            simpMessagingTemplate.convertAndSend("/topic/chat/" + payload.chatInContextOfSender().chatId(), payload);
        }
    }

    // read
    @GetMapping("/chat/{chatId}")
    public List<MessageResponse> getMessagesByChat(@PathVariable Long chatId) {
        return messageService.getMessagesByChat(chatId);
    }
}
