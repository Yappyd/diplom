package com.yappyd.messageservice.service;

import com.yappyd.messageservice.exception.ChatAccessDeniedException;
import com.yappyd.messageservice.model.ChatMessagePermission;
import com.yappyd.messageservice.repository.ChatMessagePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessagePermissionService {

    private final ChatMessagePermissionRepository permissionRepository;

    public boolean canDeleteAnyMessages(UUID chatId, UUID userId) {
        return permissionRepository.canDeleteAnyMessages(chatId, userId);
    }

    public void validateUserInChat(UUID chatId, UUID userId) {
        boolean userInChat = permissionRepository.existsByIdChatIdAndIdUserId(chatId, userId);
        if (!userInChat) {
            throw new ChatAccessDeniedException(chatId, userId);
        }
    }

    @Transactional
    public void upsertPermission(UUID chatId, UUID userId, boolean canDeleteAnyMessages) {
        ChatMessagePermission permission = permissionRepository
                .findByIdChatIdAndIdUserId(chatId, userId)
                .orElseGet(() -> new ChatMessagePermission(chatId, userId, canDeleteAnyMessages));

        permission.update(canDeleteAnyMessages);

        permissionRepository.save(permission);
    }

    public void deletePermission(UUID chatId, UUID userId) {
        permissionRepository.deleteByIdChatIdAndIdUserId(chatId, userId);
    }
}