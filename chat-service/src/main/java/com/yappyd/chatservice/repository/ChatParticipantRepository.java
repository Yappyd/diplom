package com.yappyd.chatservice.repository;

import com.yappyd.chatservice.model.ChatParticipant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatParticipantRepository {
    List<ChatParticipant> findByIdUserIdAndLeftAtIsNull(UUID userId);

    List<ChatParticipant> findByIdChatIdAndLeftAtIsNull(UUID chatId);

    Optional<ChatParticipant> findByIdChatIdAndIdUserIdAndLeftAtIsNull(UUID chatId, UUID userId);

    boolean existsByIdChatIdAndIdUserIdAndLeftAtIsNull(UUID chatId, UUID userId);
}
