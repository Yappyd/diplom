package com.yappyd.userservice.controller;

import com.yappyd.userservice.component.UserMapper;
import com.yappyd.userservice.dto.request.BatchUsersRequest;
import com.yappyd.userservice.dto.request.FindByPhoneRequest;
import com.yappyd.userservice.dto.request.UpdateUserProfileRequest;
import com.yappyd.userservice.dto.responce.BatchUsersResponse;
import com.yappyd.userservice.dto.responce.CurrentUserProfileResponse;
import com.yappyd.userservice.dto.responce.PublicUserProfileResponse;
import com.yappyd.userservice.model.User;
import com.yappyd.userservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserProfileResponse> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toCurrentUserProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<CurrentUserProfileResponse> updateCurrentUserProfile(@AuthenticationPrincipal Jwt jwt,
                                                                               @Valid @RequestBody UpdateUserProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userService.updateProfile(userId,
                request.firstName(),
                request.lastName(),
                request.tag(),
                request.phoneIsVisible());
        return ResponseEntity.ok(userMapper.toCurrentUserProfile(user));
    }

    @PostMapping("/search/phone")
    public ResponseEntity<PublicUserProfileResponse> searchByPhoneNumber(@Valid @RequestBody FindByPhoneRequest request) {
        User user = userService.getUserByPhoneNumber(request.phoneNumber());
        return ResponseEntity.ok(userMapper.toPublicUserProfile(user));
    }

    @GetMapping("/search/tag")
    public ResponseEntity<PublicUserProfileResponse> search(
            @RequestParam
            @Size(min = 5, max = 64)
            @Pattern(regexp = "^[a-zA-Z0-9_]+$")
            String tag) {
        User user = userService.getUserByTag(tag);
        return ResponseEntity.ok(userMapper.toPublicUserProfile(user));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchUsersResponse> getBatchUsers(@Valid @RequestBody BatchUsersRequest request) {
        List<User> users = userService.getBatchUsers(request.userIds());
        return ResponseEntity.ok(userMapper.toBatch(users));
    }
}
