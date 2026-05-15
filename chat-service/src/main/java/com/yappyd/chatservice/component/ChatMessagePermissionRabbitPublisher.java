package com.yappyd.chatservice.component;

import com.yappyd.chatservice.config.RabbitConfig;
import com.yappyd.chatservice.dto.event.ChatMessagePermissionDeletedEvent;
import com.yappyd.chatservice.dto.event.ChatMessagePermissionUpsertedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatMessagePermissionRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishPermissionUpserted(ChatMessagePermissionUpsertedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.CHAT_EVENTS_EXCHANGE,
                RabbitConfig.CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishPermissionDeleted(ChatMessagePermissionDeletedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHAT_EVENTS_EXCHANGE,
                RabbitConfig.CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY,
                event
        );
    }
}
