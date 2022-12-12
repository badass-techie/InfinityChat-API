package com.badasstechie.infinitychat.Auth;

public record SignupRequest(
        String username,
        String avatar,
        String password
){}
