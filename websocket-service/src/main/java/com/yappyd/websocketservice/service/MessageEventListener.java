package com.yappyd.websocketservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.yappyd.websocketservice.config.rabbit.MessageEventsRabbitConfig;
import com.yappyd.websocketservice.dto.event.MessageCreatedEvent;
import com.yappyd.websocketservice.dto.event.MessageDeletedEvent;
import com.yappyd.websocketservice.dto.event.MessageUpdatedEvent;
import com.yappyd.websocketservice.dto.websocket.WebSocketEvent;
import com.yappyd.websocketservice.enums.WebSocketEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventListener {

    private final ChatWebSocketMembershipService membershipService;
    private final WebSocketEventSender webSocketEventSender;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = MessageEventsRabbitConfig.MESSAGE_EVENTS_QUEUE)
    public void handleMessageEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (MessageEventsRabbitConfig.MESSAGE_CREATED_ROUTING_KEY.equals(routingKey)) {
                MessageCreatedEvent event = objectMapper.readValue(message.getBody(), MessageCreatedEvent.class);
                sendToChat(event.chatId(), WebSocketEventType.MESSAGE_CREATED, event);
                log.debug("MESSAGE_CREATED sent. messageId={}, chatId={}", event.messageId(), event.chatId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (MessageEventsRabbitConfig.MESSAGE_UPDATED_ROUTING_KEY.equals(routingKey)) {
                MessageUpdatedEvent event = objectMapper.readValue(message.getBody(), MessageUpdatedEvent.class);
                sendToChat(event.chatId(), WebSocketEventType.MESSAGE_UPDATED, event);
                log.debug("MESSAGE_UPDATED sent. messageId={}, chatId={}", event.messageId(), event.chatId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (MessageEventsRabbitConfig.MESSAGE_DELETED_ROUTING_KEY.equals(routingKey)) {
                MessageDeletedEvent event = objectMapper.readValue(message.getBody(), MessageDeletedEvent.class);
                sendToChat(event.chatId(), WebSocketEventType.MESSAGE_DELETED, event);
                log.debug("MESSAGE_DELETED sent. messageId={}, chatId={}", event.messageId(), event.chatId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.warn("Unknown message event routing key: {}", routingKey);
            channel.basicAck(deliveryTag, false);

        } catch (JsonProcessingException ex) {
            log.error("Failed to deserialize message event. routingKey={}", routingKey, ex);
            channel.basicReject(deliveryTag, false);

        } catch (Exception ex) {
            log.error("Failed to handle message event. routingKey={}", routingKey, ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private void sendToChat(UUID chatId, WebSocketEventType eventType, Object payload) {
        List<UUID> recipientUserIds = membershipService.findUserIdsByChatId(chatId);
        WebSocketEvent<Object> webSocketEvent = WebSocketEvent.now(eventType, payload);
        webSocketEventSender.sendToUsers(recipientUserIds, webSocketEvent);
        log.debug("WebSocket event sent to chat users. chatId={}, eventType={}, recipientsCount={}", chatId, eventType, recipientUserIds.size());
    }
}