package com.yappyd.chatservice.controller;

import com.yappyd.chatservice.dto.request.CreatePrivateChatRequest;
import com.yappyd.chatservice.dto.response.ChatListResponse;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.dto.response.CreatePrivateChatResponse;
import com.yappyd.chatservice.exception.InvalidJwtException;
import com.yappyd.chatservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/private")
    public ResponseEntity<CreatePrivateChatResponse> createPrivateChat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePrivateChatRequest request
    ) {
        UUID currentUserId = extractUserId(jwt);
        UUID chatId = chatService.createPrivateChat(currentUserId, request.targetUserId());
        return ResponseEntity.ok(new CreatePrivateChatResponse(chatId));
    }

    @GetMapping
    public ResponseEntity<ChatListResponse> getChats(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId = extractUserId(jwt);
        ChatListResponse response = chatService.getChats(currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChat(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId) {
        UUID currentUserId = extractUserId(jwt);
        ChatResponse response = chatService.getChat(currentUserId, chatId);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(Jwt jwt) {
        if (jwt == null) {
            throw new InvalidJwtException("JWT must not be null");
        }

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new InvalidJwtException("JWT subject must not be null or blank");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw new InvalidJwtException("JWT subject must be valid UUID", ex);
        }
    }
}
