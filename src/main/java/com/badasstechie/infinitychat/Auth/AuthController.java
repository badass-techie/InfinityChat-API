package com.badasstechie.infinitychat.Auth;

import com.badasstechie.infinitychat.Security.RefreshToken.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody SignupRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh_token")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest);
    }

    @GetMapping("/logout/{refreshToken}")
    public ResponseEntity<String> logout(@PathVariable String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
        return new ResponseEntity<>("You will be signed out shortly", HttpStatus.ACCEPTED);
    }
}
