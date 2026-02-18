package com.yappyd.userservice.service;

import com.rabbitmq.client.Channel;
import com.yappyd.userservice.config.RabbitConfig;
import com.yappyd.userservice.dto.rabbitmq.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class UserCreatedListener {
    private final UserService userService;

    public UserCreatedListener(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(queues = RabbitConfig.USER_CREATED_QUEUE)
    public void handleUserCreated(UserCreatedEvent event, Channel channel, Message message) throws IOException {
        userService.saveCreatedUser(event);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
