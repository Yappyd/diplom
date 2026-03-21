package com.yappyd.chatservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Entity
@Table(name = "private_chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivateChat {

    @Id
    @Column(name = "chat_id", nullable = false)
    private UUID chatId;

    @Column(name = "user_a", nullable = false)
    private UUID userA;

    @Column(name = "user_b", nullable = false)
    private UUID userB;

    public PrivateChat(UUID chatId, UUID user1, UUID user2) {
        if (chatId == null) {
            //TODO custom exception
        }
        if (user1 == null || user2 == null) {
            //TODO custom exception
        }
        if (user1.equals(user2)) {
            //TODO custom exception
        }
        this.chatId = chatId;
        if (user1.compareTo(user2) < 0) {
            this.userA = user1;
            this.userB = user2;
        } else {
            this.userA = user2;
            this.userB = user1;
        }
    }
}
