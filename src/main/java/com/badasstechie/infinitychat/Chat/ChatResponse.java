package com.badasstechie.infinitychat.Chat;

public record ChatResponse(
        Long chatId,
        String otherMemberUsername,
        String otherMemberAvatar,
        String lastMessage,
        String lastMessageTime,
        int messageCount)
{}
