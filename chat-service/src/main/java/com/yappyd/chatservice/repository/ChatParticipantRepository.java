package com.yappyd.chatservice.repository;

import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.model.ChatParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {
    List<ChatParticipant> findByIdUserId(UUID userId);

    List<ChatParticipant> findByIdChatId(UUID chatId);

    Optional<ChatParticipant> findByIdChatIdAndIdUserId(UUID chatId, UUID userId);

    boolean existsByIdChatIdAndIdUserId(UUID chatId, UUID userId);
}
