package com.badasstechie.infinitychat.Auth;

import com.badasstechie.infinitychat.AppUser.AppUser;
import com.badasstechie.infinitychat.AppUser.AppUserRepository;
import com.badasstechie.infinitychat.AppUser.AppUserAuthority;
import com.badasstechie.infinitychat.Security.JwtService;
import com.badasstechie.infinitychat.Security.RefreshToken.RefreshTokenService;
import com.badasstechie.infinitychat.Utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, AppUserRepository appUserRepository, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public ResponseEntity<String> signup(SignupRequest signupRequest) {
        AppUser appUser = AppUser.builder()
                .username(signupRequest.username())
                .password(passwordEncoder.encode(signupRequest.password()))
                .avatar(signupRequest.avatar().getBytes())
                .appUserAuthority(AppUserAuthority.USER)
                .created(Instant.now())
                .build();

        appUserRepository.save(appUser);
        return new ResponseEntity<>("Registration Successful", HttpStatus.CREATED);
    }

    public AuthResponse login(SignupRequest loginRequest) {
        try {
            Authentication authObject = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
            SecurityContextHolder.getContext().setAuthentication(authObject);
            Pair<String, Instant> jwtAndExpiration = jwtService.generateToken(authObject);

            Optional<AppUser> appUser = appUserRepository.findByUsername(loginRequest.username());
            if (appUser.isEmpty())
                throw new RuntimeException("User not found");

            return new AuthResponse(
                    jwtAndExpiration.first,
                    jwtAndExpiration.second,
                    refreshTokenService.generateRefreshToken(appUser.get()).getToken(),
                    loginRequest.username()
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect username or password");
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.username(), refreshTokenRequest.refreshToken());

        String username = refreshTokenRequest.username();
        Optional<AppUser> appUser = appUserRepository.findByUsername(username);
        if (appUser.isEmpty())
            throw new IllegalStateException("Authenticated user not found");
        String scope = appUser.get().getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Pair<String, Instant> jwtAndExpiration = jwtService.generateTokenFromUsername(username, scope);
        return new AuthResponse(
                jwtAndExpiration.first,
                jwtAndExpiration.second,
                refreshTokenRequest.refreshToken(),
                username
        );
    }

    public AppUser getUserFromAuthentication() {
        return appUserRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
