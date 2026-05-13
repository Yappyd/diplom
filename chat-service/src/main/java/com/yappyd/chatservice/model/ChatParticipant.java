package com.yappyd.chatservice.model;

import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.InvalidChatParticipantException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant {

    @EmbeddedId
    private ChatParticipantId id;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    @Column(name = "nickname", length = 255)
    private String nickname;

    @Column(name = "joined_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public ChatParticipant(UUID chatId, UUID userId, ParticipantRole role) {
        if (chatId == null || userId == null) {
            throw new InvalidChatParticipantException("chatId and userId must not be null");
        }
        if (role == null) {
            throw new InvalidChatParticipantException("participant role must not be null");
        }
        this.id = new ChatParticipantId(chatId, userId);
        this.role = role;
    }

    public void updateNickname(String nickname) {
        this.nickname = (nickname == null || nickname.isBlank()) ? null : nickname.trim();
    }

    public void changeRole(ParticipantRole role) {
        if (role == null) {
            throw new InvalidChatParticipantException("participant role must not be null");
        }
        this.role = role;
    }
}
