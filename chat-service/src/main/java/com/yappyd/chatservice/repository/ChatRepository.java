package com.yappyd.chatservice.repository;

import com.yappyd.chatservice.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findByIdInOrderByUpdatedAtDesc(Collection<UUID> ids);
}