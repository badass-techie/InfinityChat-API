package com.badasstechie.infinitychat.Message;

public record MessageResponse(
        Long messageId,
        String text,
        String senderName,
        String timeSent)
{}
