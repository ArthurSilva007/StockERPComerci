package com.nexerp.auth.api;

import com.nexerp.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login (
            @RequestHeader("X-Empresa-Id")UUID empresaId,
            @Valid @RequestBody AuthLoginRequest request
            ){
        return ResponseEntity.ok(
                authService
                        .login(empresaId,
                                request)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthLoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    authService.refresh(request.refreshToken())
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de refresh inválido",
                    ex
            );
        }
    }
}
