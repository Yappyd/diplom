package com.yappyd.websocketservice.service;

import com.yappyd.websocketservice.model.ChatWebSocketMembership;
import com.yappyd.websocketservice.repository.ChatWebSocketMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatWebSocketMembershipService {

    private final ChatWebSocketMembershipRepository membershipRepository;

    @Transactional
    public void upsertMembership(UUID chatId, UUID userId) {
        if (existsMembership(chatId, userId)) return;

        ChatWebSocketMembership membership = new ChatWebSocketMembership(chatId, userId);
        membershipRepository.save(membership);
    }

    @Transactional
    public void deleteMembership(UUID chatId, UUID userId) {
        membershipRepository.deleteByIdChatIdAndIdUserId(chatId, userId);
    }

    public List<UUID> findUserIdsByChatId(UUID chatId) {
        return membershipRepository.findUserIdsByChatId(chatId);
    }

    public boolean existsMembership(UUID chatId, UUID userId) {
        return membershipRepository.existsByIdChatIdAndIdUserId(chatId, userId);
    }
}