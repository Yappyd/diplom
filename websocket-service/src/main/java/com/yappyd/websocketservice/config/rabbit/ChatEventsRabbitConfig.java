package com.yappyd.websocketservice.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatEventsRabbitConfig {

    public static final String CHAT_EVENTS_EXCHANGE = "chat.events";
    public static final String CHAT_EVENTS_QUEUE = "websocket-service.chat-events.queue";
    public static final String CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN = "chat.message-permission.*";
    public static final String CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY = "chat.message-permission.upserted";
    public static final String CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY = "chat.message-permission.deleted";
    public static final String CHAT_CREATED_ROUTING_KEY = "chat.created";
    public static final String CHAT_UPDATED_ROUTING_KEY = "chat.updated";
    public static final String CHAT_DELETED_ROUTING_KEY = "chat.deleted";
    public static final String CHAT_PARTICIPANT_ADDED_ROUTING_KEY = "chat.participant.added";
    public static final String CHAT_PARTICIPANT_UPDATED_ROUTING_KEY = "chat.participant.updated";
    public static final String CHAT_PARTICIPANT_REMOVED_ROUTING_KEY = "chat.participant.removed";

    @Bean
    public TopicExchange chatEventsExchange() {
        return ExchangeBuilder
                .topicExchange(CHAT_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue chatEventsQueue() {
        return QueueBuilder
                .durable(CHAT_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Binding chatMessagePermissionBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN);
    }

    @Bean
    public Binding chatCreatedBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding chatUpdatedBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding chatDeletedBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding chatParticipantAddedBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_PARTICIPANT_ADDED_ROUTING_KEY);
    }

    @Bean
    public Binding chatParticipantUpdatedBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_PARTICIPANT_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding chatParticipantRemovedBinding(
            Queue chatEventsQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_PARTICIPANT_REMOVED_ROUTING_KEY);
    }
}