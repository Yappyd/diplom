package com.yappyd.messageservice.controller;

import com.yappyd.messageservice.dto.request.SendMessageRequest;
import com.yappyd.messageservice.dto.request.UpdateMessageRequest;
import com.yappyd.messageservice.dto.response.MessagePageResponse;
import com.yappyd.messageservice.dto.response.MessageResponse;
import com.yappyd.messageservice.exception.InvalidJwtException;
import com.yappyd.messageservice.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SendMessageRequest request
    ) {
        UUID userId = extractUserId(jwt);
        MessageResponse response = messageService.sendMessage(userId, request.chatId(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{messageId}")
    public MessageResponse getMessageById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID messageId
    ) {
        UUID userId = extractUserId(jwt);
        return messageService.getMessageById(userId, messageId);
    }

    @GetMapping
    public MessagePageResponse getMessagesByChat(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID chatId,
            @RequestParam(required = false) OffsetDateTime before,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        UUID userId = extractUserId(jwt);
        return messageService.getMessagesByChat(userId, chatId, before, limit);
    }

    @PutMapping("/{messageId}")
    public MessageResponse updateMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request
    ) {
        UUID userId = extractUserId(jwt);
        return messageService.updateMessage(userId, messageId, request.content());
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID messageId
    ) {
        UUID userId = extractUserId(jwt);
        messageService.deleteMessage(userId, messageId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new InvalidJwtException("JWT subject is missing");
        }

        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException ex) {
            throw new InvalidJwtException("JWT subject must be a valid UUID", ex);
        }
    }
}
