package com.yappyd.messageservice.component;

import com.yappyd.messageservice.config.WebSocketRabbitConfig;
import com.yappyd.messageservice.dto.event.MessageCreatedEvent;
import com.yappyd.messageservice.dto.event.MessageDeletedEvent;
import com.yappyd.messageservice.dto.event.MessageUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MessageRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishMessageCreated(MessageCreatedEvent event) {
        rabbitTemplate.convertAndSend(WebSocketRabbitConfig.MESSAGE_EVENTS_EXCHANGE, WebSocketRabbitConfig.MESSAGE_CREATED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishMessageUpdated(MessageUpdatedEvent event) {
        rabbitTemplate.convertAndSend(WebSocketRabbitConfig.MESSAGE_EVENTS_EXCHANGE, WebSocketRabbitConfig.MESSAGE_UPDATED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishMessageDeleted(MessageDeletedEvent event) {
        rabbitTemplate.convertAndSend(WebSocketRabbitConfig.MESSAGE_EVENTS_EXCHANGE, WebSocketRabbitConfig.MESSAGE_DELETED_ROUTING_KEY, event);
    }
}