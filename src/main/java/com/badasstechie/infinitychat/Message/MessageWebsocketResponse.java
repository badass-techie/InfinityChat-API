package com.badasstechie.infinitychat.Message;

import com.badasstechie.infinitychat.Chat.ChatResponse;

public record MessageWebsocketResponse(
        MessageResponse message,
        ChatResponse chatInContextOfSender,
        ChatResponse chatInContextOfRecipient)
{}
