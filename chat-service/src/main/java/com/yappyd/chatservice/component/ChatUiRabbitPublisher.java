package com.yappyd.chatservice.component;

import com.yappyd.chatservice.config.ChatUiRabbitConfig;
import com.yappyd.chatservice.config.RabbitCommonConfig;
import com.yappyd.chatservice.dto.event.ChatCreatedEvent;
import com.yappyd.chatservice.dto.event.ChatDeletedEvent;
import com.yappyd.chatservice.dto.event.ChatParticipantAddedEvent;
import com.yappyd.chatservice.dto.event.ChatParticipantRemovedEvent;
import com.yappyd.chatservice.dto.event.ChatParticipantUpdatedEvent;
import com.yappyd.chatservice.dto.event.ChatUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatUiRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishChatCreated(ChatCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitCommonConfig.CHAT_EVENTS_EXCHANGE, ChatUiRabbitConfig.CHAT_CREATED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishChatUpdated(ChatUpdatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitCommonConfig.CHAT_EVENTS_EXCHANGE, ChatUiRabbitConfig.CHAT_UPDATED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishChatDeleted(ChatDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitCommonConfig.CHAT_EVENTS_EXCHANGE, ChatUiRabbitConfig.CHAT_DELETED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishParticipantAdded(ChatParticipantAddedEvent event) {
        rabbitTemplate.convertAndSend(RabbitCommonConfig.CHAT_EVENTS_EXCHANGE, ChatUiRabbitConfig.CHAT_PARTICIPANT_ADDED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishParticipantUpdated(ChatParticipantUpdatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitCommonConfig.CHAT_EVENTS_EXCHANGE, ChatUiRabbitConfig.CHAT_PARTICIPANT_UPDATED_ROUTING_KEY, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publishParticipantRemoved(ChatParticipantRemovedEvent event) {
        rabbitTemplate.convertAndSend(RabbitCommonConfig.CHAT_EVENTS_EXCHANGE, ChatUiRabbitConfig.CHAT_PARTICIPANT_REMOVED_ROUTING_KEY, event);
    }
}