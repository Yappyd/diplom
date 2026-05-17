package com.yappyd.userservice.component;

import com.yappyd.userservice.config.UserRabbitConfig;
import com.yappyd.userservice.dto.event.UserProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishUserCreated(UserProfileCreatedEvent event) {
        rabbitTemplate.convertAndSend(UserRabbitConfig.USER_EVENTS_EXCHANGE, UserRabbitConfig.USER_CREATED_ROUTING_KEY, event);
    }
}