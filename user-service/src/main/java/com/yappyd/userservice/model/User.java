package com.yappyd.userservice.model;

import com.yappyd.userservice.exception.InvalidProfileDataException;
import com.yappyd.userservice.exception.ProfileAlreadyCompletedException;
import jakarta.persistence.*;
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

    @Column(name = "first_name", length = 64)
    private String firstName;

    @Column(name = "last_name", length = 64)
    private String lastName;

    @Column(name = "tag", length = 64)
    private String tag;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @Column(name = "phone_is_visible", nullable = false)
    private boolean phoneIsVisible;

    public void completeProfile(String firstName) {
        if (this.profileCompleted) throw new ProfileAlreadyCompletedException();
        if (firstName == null || firstName.isEmpty()) throw new InvalidProfileDataException("First name is empty");

        this.firstName = firstName.trim();
        this.profileCompleted = true;
    }

    public void updateProfile(String firstName, String lastName, String tag, boolean phoneIsVisible) {
        if (!this.profileCompleted) throw new InvalidProfileDataException("Profile is not completed yet");
        if (firstName == null || firstName.isEmpty()) throw new InvalidProfileDataException("First name is empty");

        this.firstName = firstName.trim();
        this.lastName = (lastName == null || lastName.isBlank()) ? null : lastName.trim();
        this.tag = (tag == null || tag.isBlank()) ? null : tag.trim();
        this.phoneIsVisible = phoneIsVisible;
    }

    @PreUpdate
    private void  preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}