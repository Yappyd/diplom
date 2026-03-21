package com.yappyd.chatservice.model;

import com.yappyd.chatservice.enums.ChatType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {

    @Id
    @Column(name = "chat_id", nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatType type;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public static Chat createPrivateChat(UUID createdBy) {
        if (createdBy == null) {
            //TODO custom exception
        }
        Chat chat = new Chat();
        chat.id = UUID.randomUUID();
        chat.type = ChatType.PRIVATE;
        chat.createdBy = createdBy;
        return chat;
    }

    public static Chat createGroupChat(UUID createdBy, String title) {
        if (title == null || title.isBlank()) {
            //TODO custom exception
        }
        if (createdBy == null) {
            //TODO custom exception
        }
        Chat chat = new Chat();
        chat.id = UUID.randomUUID();
        chat.type = ChatType.GROUP;
        chat.title = title.trim();
        chat.createdBy = createdBy;
        return chat;
    }

    public void updateTitle(String title) {
        if (this.type == ChatType.PRIVATE) {
            //TODO custom exception
        }
        if (title == null || title.isBlank()) {
            //TODO custom exception
        }
        this.title = title.trim();
    }
}
