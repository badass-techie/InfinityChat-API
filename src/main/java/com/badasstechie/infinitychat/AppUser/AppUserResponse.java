package com.badasstechie.infinitychat.AppUser;

public record AppUserResponse (
    Long appUserId,
    String username,
    String avatar,
    String created
){}
