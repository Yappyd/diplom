package com.yappyd.messageservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "chat_message_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessagePermission {

    @EmbeddedId
    private ChatMessagePermissionId id;

    @Column(name = "can_delete_any_messages", nullable = false)
    private boolean canDeleteAnyMessages;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public ChatMessagePermission(UUID chatId, UUID userId, boolean canDeleteAnyMessages) {
        this.id = new ChatMessagePermissionId(chatId, userId);
        this.canDeleteAnyMessages = canDeleteAnyMessages;
    }

    public void update(boolean canDeleteAnyMessages) {
        this.canDeleteAnyMessages = canDeleteAnyMessages;
    }

    public UUID getChatId() {
        return id.getChatId();
    }

    public UUID getUserId() {
        return id.getUserId();
    }
}