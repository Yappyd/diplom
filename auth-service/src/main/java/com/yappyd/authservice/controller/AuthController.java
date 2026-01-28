package com.yappyd.authservice.controller;

import com.yappyd.authservice.dto.LoginRequest;
import com.yappyd.authservice.dto.LoginResponse;
import com.yappyd.authservice.dto.RefreshRequest;
import com.yappyd.authservice.dto.RefreshResponse;
import com.yappyd.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest.phoneNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh (@Valid @RequestBody RefreshRequest refreshRequest) {
        RefreshResponse response = authService.refresh(refreshRequest.refreshToken());
        return ResponseEntity.ok(response);
    }
}
