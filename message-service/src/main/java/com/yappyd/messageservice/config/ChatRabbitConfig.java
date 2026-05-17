package com.yappyd.messageservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatRabbitConfig {

    public static final String CHAT_EVENTS_EXCHANGE = "chat.events";

    public static final String CHAT_MESSAGE_PERMISSION_QUEUE = "message-service.chat-message-permission.queue";
    public static final String CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN = "chat.message-permission.*";
    public static final String CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY = "chat.message-permission.upserted";
    public static final String CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY = "chat.message-permission.deleted";

    @Bean
    public Queue chatMessagePermissionQueue() {
        return QueueBuilder
                .durable(CHAT_MESSAGE_PERMISSION_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange chatEventsExchange() {
        return ExchangeBuilder
                .topicExchange(CHAT_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding chatMessagePermissionBinding(
            Queue chatMessagePermissionQueue,
            TopicExchange chatEventsExchange
    ) {
        return BindingBuilder
                .bind(chatMessagePermissionQueue)
                .to(chatEventsExchange)
                .with(CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN);
    }
}