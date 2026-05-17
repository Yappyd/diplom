package com.yappyd.chatservice.controller;

import com.yappyd.chatservice.dto.request.*;
import com.yappyd.chatservice.dto.response.ChatListResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantsResponse;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.exception.InvalidJwtException;
import com.yappyd.chatservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/private")
    public ResponseEntity<ChatResponse> createPrivateChat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePrivateChatRequest request
    ) {
        UUID currentUserId = extractUserId(jwt);
        ChatResponse response = chatService.createPrivateChat(currentUserId, request.targetUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatResponse> createGroupChat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateGroupChatRequest request
    ) {
        UUID currentUserId = extractUserId(jwt);
        ChatResponse response = chatService.createGroupChat(currentUserId, request.title(), request.participantIds());
        return ResponseEntity.ok(response);
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

    @PatchMapping("/{chatId}")
    public ResponseEntity<ChatResponse> updateChat(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId,
            @RequestBody UpdateChatRequest request
    ) {
        UUID currentUserId = extractUserId(jwt);
        ChatResponse response = chatService.updateChat(currentUserId, chatId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteGroupChat(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId
    ) {
        UUID currentUserId = extractUserId(jwt);
        chatService.deleteGroupChat(currentUserId, chatId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatId}/participants")
    public ResponseEntity<ChatParticipantsResponse> getChatParticipants(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId) {
        UUID currentUserId = extractUserId(jwt);
        ChatParticipantsResponse response = chatService.getChatParticipants(currentUserId, chatId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<ChatParticipantsResponse> addParticipant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId,
            @Valid @RequestBody AddParticipantRequest request) {
        UUID currentUserId = extractUserId(jwt);
        ChatParticipantsResponse response = chatService.addParticipant(currentUserId, chatId, request.userId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{chatId}/participants/{userId}")
    public ResponseEntity<?> removeParticipant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId,
            @PathVariable UUID userId,
            @RequestParam(required = false) UUID newOwnerId
    ) {
        UUID currentUserId = extractUserId(jwt);
        Optional<ChatParticipantsResponse> response = chatService.removeParticipant(currentUserId, chatId, userId, newOwnerId);
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{chatId}/participants/{userId}")
    public ResponseEntity<ChatParticipantResponse> updateParticipant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID chatId,
            @PathVariable UUID userId,
            @RequestBody UpdateChatParticipantRequest request
    ) {
        UUID currentUserId = extractUserId(jwt);
        ChatParticipantResponse response = chatService.updateParticipant(currentUserId, chatId, userId, request);
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
