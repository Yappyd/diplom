package com.yappyd.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.yappyd.userservice.config.AuthRabbitConfig;
import com.yappyd.userservice.dto.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = AuthRabbitConfig.AUTH_USER_CREATED_QUEUE)
    public void handleUserCreatedEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (!AuthRabbitConfig.USER_CREATED_ROUTING_KEY.equals(routingKey)) {
                log.warn("Unsupported auth event routingKey={}", routingKey);
                channel.basicReject(deliveryTag, false);
                return;
            }

            UserCreatedEvent event = objectMapper.readValue(message.getBody(), UserCreatedEvent.class);
            userService.saveCreatedUser(event);
            channel.basicAck(deliveryTag, false);
            log.info("Handled user created event: userId={}, routingKey={}", event.userId(), routingKey);

        } catch (Exception e) {
            log.error("Failed to handle auth event: routingKey={}", routingKey, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}