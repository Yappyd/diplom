package com.yappyd.authservice.service;

import com.yappyd.authservice.dto.response.LoginResponse;
import com.yappyd.authservice.dto.response.RefreshResponse;
import com.yappyd.authservice.exception.UserDeactivatedException;
import com.yappyd.authservice.exception.UserNotFoundException;
import com.yappyd.authservice.model.AuthUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public LoginResponse login(String phoneNumber) {
        AuthUser user = userService
                .getUserByPhoneNumber(phoneNumber)
                .orElseGet(() -> userService.saveVerifiedUser(phoneNumber));

        if (!user.isActive()) {
            throw new UserDeactivatedException("User is inactive: " + user.getId());
        }

        Instant now = Instant.now();
        UUID userId = user.getId();

        return new LoginResponse(
                jwtService.generateAccessToken(userId, now),
                jwtService.generateRefreshToken(userId, now),
                now.plus(jwtService.getAccessTtl())
        );
    }

    public RefreshResponse refresh(String refreshToken) {
        UUID userId = jwtService.parseRefreshToken(refreshToken);

        AuthUser user = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (!user.isActive()) {
            throw new UserDeactivatedException("User is inactive: " + userId);
        }

        Instant now = Instant.now();

        return new RefreshResponse(
                jwtService.generateAccessToken(userId, now),
                jwtService.generateRefreshToken(userId, now),
                now.plus(jwtService.getAccessTtl())
        );
    }
}