package com.yappyd.websocketservice.repository;

import com.yappyd.websocketservice.model.ChatWebSocketMembership;
import com.yappyd.websocketservice.model.ChatWebSocketMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatWebSocketMembershipRepository extends JpaRepository<ChatWebSocketMembership, ChatWebSocketMembershipId> {

    boolean existsByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO chat_websocket_memberships (chat_id, user_id)
            VALUES (:chatId, :userId)
            ON CONFLICT (chat_id, user_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("chatId") UUID chatId,
            @Param("userId") UUID userId
    );

    void deleteByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    @Query("""
            SELECT m.id.userId
            FROM ChatWebSocketMembership m
            WHERE m.id.chatId = :chatId
            """)
    List<UUID> findUserIdsByChatId(UUID chatId);

    @Query("""
        SELECT DISTINCT m.id.userId
        FROM ChatWebSocketMembership m
        WHERE m.id.chatId IN (
            SELECT own.id.chatId
            FROM ChatWebSocketMembership own
            WHERE own.id.userId = :userId
        )
        """)
    List<UUID> findUserIdsSharingChatsWithUserId(UUID userId);
}