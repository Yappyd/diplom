package com.yappyd.websocketservice.service;

import com.yappyd.websocketservice.dto.websocket.ChatAccessRevokedPayload;
import com.yappyd.websocketservice.dto.websocket.WebSocketEvent;
import com.yappyd.websocketservice.enums.WebSocketEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventSender {

    private static final String USER_EVENTS_DESTINATION = "/queue/events";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(UUID userId, WebSocketEvent<?> event) {
        messagingTemplate.convertAndSendToUser(userId.toString(), USER_EVENTS_DESTINATION, event);
        log.debug("WebSocket event sent to user. userId={}, eventType={}", userId, event.type());
    }

    public void sendToUsers(Collection<UUID> userIds, WebSocketEvent<?> event) {
        for (UUID userId : userIds) {
            sendToUser(userId, event);
        }
    }

    public void sendChatAccessRevoked(UUID userId, UUID chatId) {
        ChatAccessRevokedPayload payload = new ChatAccessRevokedPayload(chatId);
        WebSocketEvent<ChatAccessRevokedPayload> event = WebSocketEvent.now(WebSocketEventType.CHAT_ACCESS_REVOKED, payload);
        sendToUser(userId, event);
    }
}