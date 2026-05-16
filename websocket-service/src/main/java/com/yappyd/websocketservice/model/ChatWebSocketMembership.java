package com.yappyd.websocketservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "chat_websocket_memberships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatWebSocketMembership {

    @EmbeddedId
    private ChatWebSocketMembershipId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ChatWebSocketMembership(UUID chatId, UUID userId) {
        this.id = new ChatWebSocketMembershipId(chatId, userId);
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public UUID getChatId() {
        return id.getChatId();
    }

    public UUID getUserId() {
        return id.getUserId();
    }
}