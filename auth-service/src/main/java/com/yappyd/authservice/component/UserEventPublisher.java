package com.yappyd.authservice.component;

import com.yappyd.authservice.dto.rabbitmq.UserCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreatedEvent(UserCreatedEvent event) {
        rabbitTemplate.convertAndSend("auth.events", "user.created", event);
    }

}
