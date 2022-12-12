package com.badasstechie.infinitychat.Auth;

import java.time.Instant;

public record AuthResponse (
    String accessToken,
    Instant expiresAt,
    String refreshToken,
    String username
){}
