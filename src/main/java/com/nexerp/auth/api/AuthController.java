package com.nexerp.auth.api;

import com.nexerp.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
