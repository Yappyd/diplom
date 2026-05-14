package com.yappyd.messageservice.model;

import com.yappyd.messageservice.exception.InvalidMessageException;
import com.yappyd.messageservice.exception.MessageAlreadyDeletedException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private static final int MAX_CONTENT_LENGTH = 4000;

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

    public static Message create(UUID chatId, UUID senderId, String content) {
        if (chatId == null) {
            throw new InvalidMessageException("chatId must not be null");
        }

        if (senderId == null) {
            throw new InvalidMessageException("senderId must not be null");
        }

        Message message = new Message();
        message.messageId = UUID.randomUUID();
        message.chatId = chatId;
        message.senderId = senderId;
        message.content = validateContent(content);
        message.createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        return message;
    }

    public void updateContent(String content) {
        if (isDeleted()) {
            throw new MessageAlreadyDeletedException(messageId);
        }

        this.content = validateContent(content);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new MessageAlreadyDeletedException(messageId);
        }

        this.deletedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public boolean isEdited() {
        return updatedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    private static String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidMessageException("Message content must not be blank");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidMessageException(
                    "Message content must not exceed " + MAX_CONTENT_LENGTH + " characters"
            );
        }

        return content;
    }
}