package com.badasstechie.infinitychat.Security.RefreshToken;

import com.badasstechie.infinitychat.AppUser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByOwnerUsernameAndToken(String username, String token);
    void deleteByToken(String token);
    void deleteAllByOwner(AppUser appUser);
}
