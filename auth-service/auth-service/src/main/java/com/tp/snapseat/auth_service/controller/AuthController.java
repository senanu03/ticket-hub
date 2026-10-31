package com.tp.snapseat.auth_service.controller;


import com.tp.snapseat.auth_service.dto.LoginRequest;
import com.tp.snapseat.auth_service.dto.LoginResponse;
import com.tp.snapseat.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}