package com.badasstechie.infinitychat.Chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@PreAuthorize("hasAuthority('SCOPE_USER')")
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // read
    @GetMapping()
    public List<ChatResponse> getYourChats() {
        return chatService.getYourChats();
    }

    @GetMapping("/{id}")
    public ChatResponse getChat(@PathVariable Long id) {
        return chatService.getChat(id);
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteChat(@PathVariable Long id) {
        return chatService.deleteChat(id);
    }
}
