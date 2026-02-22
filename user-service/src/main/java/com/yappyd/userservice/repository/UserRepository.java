package com.yappyd.userservice.repository;

import com.yappyd.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = """
            SELECT * FROM users 
            WHERE phone_number = :phoneNumber 
              AND phone_is_visible = true 
              AND profile_completed = true 
            """, nativeQuery = true)
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query(value = """
        SELECT * FROM users
        WHERE LOWER(tag) = LOWER(:tag)
          AND phone_is_visible = true
          AND profile_completed = true
        """, nativeQuery = true)
    Optional<User> findByTag(String tag);

    @Modifying
    @Query(value = """
            INSERT INTO users (user_id, phone_number, created_at)
            VALUES ( :userId, :phoneNumber, :createdAt)
            ON CONFLICT (user_id) DO NOTHING 
            """, nativeQuery = true)
    int saveCreatedUser(UUID userId, String phoneNumber, OffsetDateTime createdAt);
}
