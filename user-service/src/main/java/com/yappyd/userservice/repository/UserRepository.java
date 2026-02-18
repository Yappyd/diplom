package com.yappyd.userservice.repository;

import com.yappyd.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhoneNumberAndPhoneIsVisibleTrue(String phoneNumber);

    Optional<User> findByTagIgnoreCase(String tag);

    @Modifying
    @Query(value = """
            INSERT INTO users (user_id, phone_number, phone_is_visible, created_at)
            VALUES ( :userId, :phoneNumber, true, :createdAt)
            ON CONFLICT (user_id) DO NOTHING 
            """, nativeQuery = true)
    int saveCreatedUser(UUID userId, String phoneNumber, OffsetDateTime createdAt);
}
