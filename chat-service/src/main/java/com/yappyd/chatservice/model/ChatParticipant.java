package com.yappyd.chatservice.model;

import com.yappyd.chatservice.enums.ParticipantRole;
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

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public ChatParticipant(UUID chatId, UUID userId, ParticipantRole role) {
        if (chatId == null || userId == null) {
            //TODO custom exception
        }
        if (role == null) {
            //TODO custom exception
        }
        this.id = new ChatParticipantId(chatId, userId);
        this.role = role;
    }

    public void updateProfile(ParticipantRole role, String nickname) {
        if (role == null) {
            //TODO custom exception
        }
        this.role = role;
        this.nickname = (nickname == null || nickname.isBlank()) ? null : nickname.trim();
    }

    public void leave() {
        if (this.leftAt != null) {
            //TODO exception
        }
        this.leftAt = OffsetDateTime.now();
    }

    public void rejoin() {
        if (this.leftAt == null) {
            //TODO exception
        }
        this.leftAt = null;
    }
}
