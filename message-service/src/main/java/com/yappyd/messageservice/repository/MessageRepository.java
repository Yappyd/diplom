package com.yappyd.messageservice.repository;

import com.yappyd.messageservice.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByChatIdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    List<Message> findByChatIdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID chatId, OffsetDateTime before, Pageable pageable);
}