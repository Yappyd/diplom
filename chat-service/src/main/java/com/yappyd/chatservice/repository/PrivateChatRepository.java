package com.yappyd.chatservice.repository;

import com.yappyd.chatservice.model.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrivateChatRepository extends JpaRepository<PrivateChat, UUID> {
    Optional<PrivateChat> findByUserAAndUserB(UUID userA, UUID userB);
}