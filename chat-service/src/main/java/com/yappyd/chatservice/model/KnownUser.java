package com.yappyd.chatservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "known_users")
public class KnownUser {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected KnownUser() {
    }

    public KnownUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}