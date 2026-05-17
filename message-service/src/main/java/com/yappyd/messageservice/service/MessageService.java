package com.yappyd.messageservice.service;

import com.yappyd.messageservice.component.MessageEventPublisher;
import com.yappyd.messageservice.dto.response.MessagePageResponse;
import com.yappyd.messageservice.dto.response.MessageResponse;
import com.yappyd.messageservice.exception.ChatAccessDeniedException;
import com.yappyd.messageservice.exception.MessageAccessDeniedException;
import com.yappyd.messageservice.exception.MessageNotFoundException;
import com.yappyd.messageservice.model.Message;
import com.yappyd.messageservice.repository.MessageRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final ChatMessagePermissionService permissionService;
    private final MessageRepository messageRepository;
    private final MessageEventPublisher messageEventPublisher;

    @Transactional
    public MessageResponse sendMessage(UUID userId, UUID chatId, String content) {
        permissionService.validateUserInChat(chatId, userId);
        Message message = Message.create(chatId, userId, content);
        messageRepository.save(message);
        messageEventPublisher.publishMessageCreated(message);
        return MessageResponse.from(message);
    }

    public MessageResponse getMessageById(UUID userId, UUID messageId) {
        Message message = getMessageOrThrow(messageId);

        permissionService.validateUserInChat(message.getChatId(), userId);

        return MessageResponse.from(message);
    }

    public MessagePageResponse getMessagesByChat(UUID userId, UUID chatId, OffsetDateTime before, int limit) {
        permissionService.validateUserInChat(chatId, userId);

        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Message> messages;

        if (before == null) {
            messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageRequest);
        } else {
            messages = messageRepository.findByChatIdAndCreatedAtBeforeOrderByCreatedAtDesc(chatId, before, pageRequest);
        }

        boolean hasMore = messages.size() > limit;
        if (hasMore) {
            messages = messages.subList(0, limit);
        }

        List<MessageResponse> items = messages.stream().map(MessageResponse::from).toList();

        OffsetDateTime nextBefore = null;
        if (hasMore && !messages.isEmpty()) {
            nextBefore = messages.get(messages.size() - 1).getCreatedAt();
        }

        return new MessagePageResponse(items, limit, hasMore, nextBefore);
    }

    @Transactional
    public MessageResponse updateMessage(UUID userId, UUID messageId, String content) {
        Message message = getMessageOrThrow(messageId);
        permissionService.validateUserInChat(message.getChatId(), userId);

        if (!message.getSenderId().equals(userId)) {
            throw new MessageAccessDeniedException(messageId);
        }

        message.updateContent(content);
        messageEventPublisher.publishMessageUpdated(message);
        return MessageResponse.from(message);
    }

    @Transactional
    public void deleteMessage(UUID userId, UUID messageId) {
        Message message = getMessageOrThrow(messageId);
        permissionService.validateUserInChat(message.getChatId(), userId);
        if (message.getSenderId().equals(userId)) {
            message.softDelete();
            return;
        }

        boolean canDeleteAnyMessages = permissionService.canDeleteAnyMessages(message.getChatId(), userId);
        if (!canDeleteAnyMessages) {
            throw new MessageAccessDeniedException(messageId);
        }

        message.softDelete();
        messageEventPublisher.publishMessageDeleted(message);
    }

    private Message getMessageOrThrow(UUID messageId) {
        return messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException(messageId));
    }
}
