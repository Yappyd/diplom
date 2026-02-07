package com.yappyd.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Setter
    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Setter
    @Column(name = "last_name", length = 64)
    private String lastName;

    @Setter
    @Column(name = "tag", length = 64)
    private String tag;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false, insertable = false)
    private OffsetDateTime updatedAt;

    @Setter
    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @Setter
    @Column(name = "phone_is_visible", nullable = false)
    private boolean phoneIsVisible;

    public User(UUID id, String phoneNumber) {
        this.id = id;
        this.phoneNumber = phoneNumber;
    }
}