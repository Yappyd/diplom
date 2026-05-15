package com.yappyd.messageservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessagePermissionId implements Serializable {

    @Column(name = "chat_id", nullable = false)
    private UUID chatId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public ChatMessagePermissionId(UUID chatId, UUID userId) {
        this.chatId = chatId;
        this.userId = userId;
    }
}