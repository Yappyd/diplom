package com.yappyd.websocketservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.yappyd.websocketservice.config.rabbit.ChatEventsRabbitConfig;
import com.yappyd.websocketservice.dto.event.ChatParticipantUpdatedEvent;
import com.yappyd.websocketservice.dto.event.ChatUpdatedEvent;
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
public class ChatUiEventListener {

    private final ChatWebSocketMembershipService membershipService;
    private final WebSocketEventSender webSocketEventSender;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = ChatEventsRabbitConfig.CHAT_UI_EVENTS_QUEUE)
    public void handleChatUiEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (ChatEventsRabbitConfig.CHAT_UPDATED_ROUTING_KEY.equals(routingKey)) {
                ChatUpdatedEvent event = objectMapper.readValue(message.getBody(), ChatUpdatedEvent.class);
                sendToChat(event.chatId(), WebSocketEventType.CHAT_UPDATED, event);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (ChatEventsRabbitConfig.CHAT_PARTICIPANT_UPDATED_ROUTING_KEY.equals(routingKey)) {
                ChatParticipantUpdatedEvent event = objectMapper.readValue(message.getBody(), ChatParticipantUpdatedEvent.class);
                sendToChat(event.chatId(), WebSocketEventType.CHAT_PARTICIPANT_UPDATED, event);
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.warn("Unknown chat UI event routing key: {}", routingKey);
            channel.basicAck(deliveryTag, false);

        } catch (JsonProcessingException ex) {
            log.error("Failed to deserialize chat UI event. routingKey={}", routingKey, ex);
            channel.basicReject(deliveryTag, false);

        } catch (Exception ex) {
            log.error("Failed to handle chat UI event. routingKey={}", routingKey, ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private void sendToChat(UUID chatId, WebSocketEventType eventType, Object payload) {
        List<UUID> recipientUserIds = membershipService.findUserIdsByChatId(chatId);
        WebSocketEvent<Object> webSocketEvent = WebSocketEvent.now(eventType, payload);
        webSocketEventSender.sendToUsers(recipientUserIds, webSocketEvent);
        log.debug("Chat UI event sent. chatId={}, eventType={}, recipientsCount={}", chatId, eventType, recipientUserIds.size()
        );
    }
}