package com.badasstechie.infinitychat.Security;

import com.badasstechie.infinitychat.Utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final JwtEncoder encoder;

    @Autowired
    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Generates a JWT token for the given authentication object
     *
     * @param authentication the authentication object
     * @return Pair<> of token and expiration time
     */
    public Pair<String, Instant> generateToken(Authentication authentication) {
        String username = authentication.getName();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return generateTokenFromUsername(username, scope);
    }

    public Pair<String, Instant> generateTokenFromUsername(String username, String scope) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(username)
                .claim("scope", scope)
                .build();

        String token = this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new Pair<>(token, claims.getExpiresAt());
    }
}
