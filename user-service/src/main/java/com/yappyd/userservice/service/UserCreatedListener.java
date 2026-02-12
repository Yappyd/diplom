package com.yappyd.userservice.service;

import com.yappyd.userservice.config.RabbitConfig;
import com.yappyd.userservice.dto.rabbitmq.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserCreatedListener {

    @RabbitListener(queues = RabbitConfig.USER_CREATED_QUEUE)
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("handle user", event);
    }

}
