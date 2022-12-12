package com.badasstechie.infinitychat.Auth;

public record RefreshTokenRequest (
        String username,
        String refreshToken
){}
