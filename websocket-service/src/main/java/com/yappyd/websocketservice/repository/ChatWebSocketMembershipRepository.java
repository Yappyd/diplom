package com.yappyd.websocketservice.repository;

import com.yappyd.websocketservice.model.ChatWebSocketMembership;
import com.yappyd.websocketservice.model.ChatWebSocketMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatWebSocketMembershipRepository extends JpaRepository<ChatWebSocketMembership, ChatWebSocketMembershipId> {

    Optional<ChatWebSocketMembership> findByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    boolean existsByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    void deleteByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    @Query("""
            SELECT m.id.userId
            FROM ChatWebSocketMembership m
            WHERE m.id.chatId = :chatId
            """)
    List<UUID> findUserIdsByChatId(UUID chatId);

    @Query("""
            SELECT m.id.chatId
            FROM ChatWebSocketMembership m
            WHERE m.id.userId = :userId
            """)
    List<UUID> findChatIdsByUserId(UUID userId);
}