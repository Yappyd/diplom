package com.yappyd.websocketservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.yappyd.websocketservice.config.RabbitConfig;
import com.yappyd.websocketservice.dto.event.ChatMessagePermissionDeletedEvent;
import com.yappyd.websocketservice.dto.event.ChatMessagePermissionUpsertedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePermissionListener {

    private final ChatWebSocketMembershipService membershipService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.CHAT_MESSAGE_PERMISSION_QUEUE)
    public void handleChatMessagePermissionEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (RabbitConfig.CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY.equals(routingKey)) {
                ChatMessagePermissionUpsertedEvent event = objectMapper.readValue(message.getBody(), ChatMessagePermissionUpsertedEvent.class);

                membershipService.upsertMembership(event.chatId(), event.userId());

                channel.basicAck(deliveryTag, false);
                return;
            }

            if (RabbitConfig.CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY.equals(routingKey)) {
                ChatMessagePermissionDeletedEvent event = objectMapper.readValue(message.getBody(), ChatMessagePermissionDeletedEvent.class);

                membershipService.deleteMembership(event.chatId(), event.userId());

                channel.basicAck(deliveryTag, false);
                return;
            }

            log.warn("Unknown chat message permission routing key: {}", routingKey);
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {
            log.error("Failed to handle chat message permission event. routingKey={}", routingKey, ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}