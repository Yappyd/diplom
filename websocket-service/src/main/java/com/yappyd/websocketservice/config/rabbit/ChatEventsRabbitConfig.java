package com.yappyd.websocketservice.config.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatEventsRabbitConfig {

    public static final String CHAT_EVENTS_EXCHANGE = "chat.events";
    public static final String CHAT_MESSAGE_PERMISSION_QUEUE = "websocket-service.chat-message-permission.queue";
    public static final String CHAT_UI_EVENTS_QUEUE = "websocket-service.chat-ui-events.queue";
    public static final String CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN = "chat.message-permission.*";
    public static final String CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY = "chat.message-permission.upserted";
    public static final String CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY = "chat.message-permission.deleted";
    public static final String CHAT_UPDATED_ROUTING_KEY = "chat.updated";
    public static final String CHAT_PARTICIPANT_UPDATED_ROUTING_KEY = "chat.participant.updated";

    @Bean
    public TopicExchange chatEventsExchange() {
        return ExchangeBuilder
                .topicExchange(CHAT_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue chatMessagePermissionQueue() {
        return QueueBuilder
                .durable(CHAT_MESSAGE_PERMISSION_QUEUE)
                .build();
    }

    @Bean
    public Queue chatUiEventsQueue() {
        return QueueBuilder
                .durable(CHAT_UI_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Binding chatMessagePermissionBinding(Queue chatMessagePermissionQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder
                .bind(chatMessagePermissionQueue)
                .to(chatEventsExchange)
                .with(CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN);
    }

    @Bean
    public Binding chatUpdatedBinding(Queue chatUiEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder
                .bind(chatUiEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding chatParticipantUpdatedBinding(Queue chatUiEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder
                .bind(chatUiEventsQueue)
                .to(chatEventsExchange)
                .with(CHAT_PARTICIPANT_UPDATED_ROUTING_KEY);
    }
}