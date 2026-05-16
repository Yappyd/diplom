package com.yappyd.websocketservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.yappyd.websocketservice.config.rabbit.UserEventsRabbitConfig;
import com.yappyd.websocketservice.dto.event.UserProfileUpdatedEvent;
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
public class UserEventListener {

    private final ChatWebSocketMembershipService membershipService;
    private final WebSocketEventSender webSocketEventSender;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = UserEventsRabbitConfig.USER_EVENTS_QUEUE)
    public void handleUserEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (UserEventsRabbitConfig.USER_PROFILE_UPDATED_ROUTING_KEY.equals(routingKey)) {
                UserProfileUpdatedEvent event = objectMapper.readValue(message.getBody(), UserProfileUpdatedEvent.class);
                handleUserProfileUpdated(event);
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.warn("Unknown user event routing key: {}", routingKey);
            channel.basicAck(deliveryTag, false);

        } catch (JsonProcessingException ex) {
            log.error("Failed to deserialize user event. routingKey={}", routingKey, ex);
            channel.basicReject(deliveryTag, false);

        } catch (Exception ex) {
            log.error("Failed to handle user event. routingKey={}", routingKey, ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        List<UUID> recipientUserIds = membershipService.findUserIdsSharingChatsWithUserId(event.userId());
        WebSocketEvent<UserProfileUpdatedEvent> webSocketEvent = WebSocketEvent.now(WebSocketEventType.USER_PROFILE_UPDATED, event);
        webSocketEventSender.sendToUsers(recipientUserIds, webSocketEvent);
        log.debug("USER_PROFILE_UPDATED sent. updatedUserId={}, recipientsCount={}", event.userId(), recipientUserIds.size());
    }
}