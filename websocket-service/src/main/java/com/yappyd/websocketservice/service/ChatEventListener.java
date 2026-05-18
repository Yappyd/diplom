package com.yappyd.websocketservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.yappyd.websocketservice.config.rabbit.ChatEventsRabbitConfig;
import com.yappyd.websocketservice.dto.event.ChatCreatedEvent;
import com.yappyd.websocketservice.dto.event.ChatDeletedEvent;
import com.yappyd.websocketservice.dto.event.ChatMessagePermissionDeletedEvent;
import com.yappyd.websocketservice.dto.event.ChatMessagePermissionUpsertedEvent;
import com.yappyd.websocketservice.dto.event.ChatParticipantAddedEvent;
import com.yappyd.websocketservice.dto.event.ChatParticipantRemovedEvent;
import com.yappyd.websocketservice.dto.event.ChatParticipantUpdatedEvent;
import com.yappyd.websocketservice.dto.event.ChatUpdatedEvent;
import com.yappyd.websocketservice.dto.websocket.ChatDeletedPayload;
import com.yappyd.websocketservice.dto.websocket.WebSocketEvent;
import com.yappyd.websocketservice.enums.WebSocketEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener {

    private final ChatWebSocketMembershipService membershipService;
    private final WebSocketEventSender webSocketEventSender;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = ChatEventsRabbitConfig.CHAT_EVENTS_QUEUE)
    public void handleChatEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (ChatEventsRabbitConfig.CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY.equals(routingKey)) {
                handleChatMessagePermissionUpserted(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY.equals(routingKey)) {
                handleChatMessagePermissionDeleted(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_CREATED_ROUTING_KEY.equals(routingKey)) {
                handleChatCreated(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_UPDATED_ROUTING_KEY.equals(routingKey)) {
                handleChatUpdated(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_DELETED_ROUTING_KEY.equals(routingKey)) {
                handleChatDeleted(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_PARTICIPANT_ADDED_ROUTING_KEY.equals(routingKey)) {
                handleChatParticipantAdded(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_PARTICIPANT_UPDATED_ROUTING_KEY.equals(routingKey)) {
                handleChatParticipantUpdated(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_PARTICIPANT_REMOVED_ROUTING_KEY.equals(routingKey)) {
                handleChatParticipantRemoved(message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.warn("Unknown chat event routing key: {}", routingKey);
            channel.basicAck(deliveryTag, false);

        } catch (JsonProcessingException ex) {
            log.error("Failed to deserialize chat event. routingKey={}", routingKey, ex);
            channel.basicReject(deliveryTag, false);

        } catch (Exception ex) {
            log.error("Failed to handle chat event. routingKey={}", routingKey, ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private void handleChatMessagePermissionUpserted(Message message) throws IOException {
        ChatMessagePermissionUpsertedEvent event = objectMapper.readValue(message.getBody(), ChatMessagePermissionUpsertedEvent.class);
        membershipService.upsertMembership(event.chatId(), event.userId());
        log.debug("Chat websocket membership upserted. chatId={}, userId={}", event.chatId(), event.userId());
    }

    private void handleChatMessagePermissionDeleted(Message message) throws IOException {
        ChatMessagePermissionDeletedEvent event = objectMapper.readValue(message.getBody(), ChatMessagePermissionDeletedEvent.class);
        webSocketEventSender.sendChatAccessRevoked(event.userId(), event.chatId());
        membershipService.deleteMembership(event.chatId(), event.userId());
        log.debug("Chat websocket membership deleted. chatId={}, userId={}", event.chatId(), event.userId());
    }

    private void handleChatCreated(Message message) throws IOException {
        ChatCreatedEvent event = objectMapper.readValue(message.getBody(), ChatCreatedEvent.class);
        sendToUsers(event.participantIds(), WebSocketEventType.CHAT_CREATED, event);
        log.debug("CHAT_CREATED sent. chatId={}, recipientsCount={}", event.chatId(), event.participantIds().size());
    }

    private void handleChatUpdated(Message message) throws IOException {
        ChatUpdatedEvent event = objectMapper.readValue(message.getBody(), ChatUpdatedEvent.class);
        sendToChat(event.chatId(), WebSocketEventType.CHAT_UPDATED, event);
    }

    private void handleChatDeleted(Message message) throws IOException {
        ChatDeletedEvent event = objectMapper.readValue(message.getBody(), ChatDeletedEvent.class);
        ChatDeletedPayload payload = new ChatDeletedPayload(event.chatId());
        sendToUsers(event.participantIds(), WebSocketEventType.CHAT_DELETED, payload);
        membershipService.deleteMembershipsByChatId(event.chatId());
        log.debug("CHAT_DELETED sent. chatId={}, recipientsCount={}", event.chatId(), event.participantIds().size());
    }

    private void handleChatParticipantAdded(Message message) throws IOException {
        ChatParticipantAddedEvent event = objectMapper.readValue(message.getBody(), ChatParticipantAddedEvent.class);
        sendToChat(event.chatId(), WebSocketEventType.CHAT_PARTICIPANT_ADDED, event);
    }

    private void handleChatParticipantUpdated(Message message) throws IOException {
        ChatParticipantUpdatedEvent event = objectMapper.readValue(message.getBody(), ChatParticipantUpdatedEvent.class);
        sendToChat(event.chatId(), WebSocketEventType.CHAT_PARTICIPANT_UPDATED, event);
    }

    private void handleChatParticipantRemoved(Message message) throws IOException {
        ChatParticipantRemovedEvent event = objectMapper.readValue(message.getBody(), ChatParticipantRemovedEvent.class);
        sendToChat(event.chatId(), WebSocketEventType.CHAT_PARTICIPANT_REMOVED, event);
    }

    private void sendToChat(UUID chatId, WebSocketEventType eventType, Object payload) {
        List<UUID> recipientUserIds = membershipService.findUserIdsByChatId(chatId);
        sendToUsers(recipientUserIds, eventType, payload);
        log.debug("Chat event sent. chatId={}, eventType={}, recipientsCount={}", chatId, eventType, recipientUserIds.size());
    }

    private void sendToUsers(Collection<UUID> userIds, WebSocketEventType eventType, Object payload) {
        WebSocketEvent<Object> webSocketEvent = WebSocketEvent.now(eventType, payload);
        webSocketEventSender.sendToUsers(userIds, webSocketEvent);
    }
}