package com.yappyd.messageservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID messageId;

    @Column(name = "chat_id", nullable = false, updatable = false)
    private UUID chatId;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    private Message(UUID chatId, UUID senderId, String content) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
    }

    public static Message create(UUID chatId, UUID senderId, String content) {
        return new Message(chatId, senderId, content);
    }

    @PrePersist
    private void prePersist() {
        if (messageId == null) {
            messageId = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public boolean isEdited() {
        return updatedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}