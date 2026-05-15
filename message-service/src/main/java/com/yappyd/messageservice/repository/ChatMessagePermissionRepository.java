package com.yappyd.messageservice.repository;

import com.yappyd.messageservice.model.ChatMessagePermission;
import com.yappyd.messageservice.model.ChatMessagePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ChatMessagePermissionRepository extends JpaRepository<ChatMessagePermission, ChatMessagePermissionId> {

    Optional<ChatMessagePermission> findByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    @Query("""
            SELECT COUNT(p) > 0
            FROM ChatMessagePermission p
            WHERE p.id.chatId = :chatId
              AND p.id.userId = :userId
              AND p.canDeleteAnyMessages = true
            """)
    boolean canDeleteAnyMessages(UUID chatId, UUID userId);

    void deleteByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    boolean existsByIdChatIdAndIdUserId(UUID chatId, UUID userId);
}