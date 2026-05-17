package com.yappyd.chatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRabbitConfig {

    public static final String USER_EVENTS_EXCHANGE = "user.events";
    public static final String CHAT_SERVICE_USER_EVENTS_QUEUE = "chat-service.user-events.queue";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";

    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder
                .topicExchange(USER_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue chatServiceUserEventsQueue() {
        return QueueBuilder
                .durable(CHAT_SERVICE_USER_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Binding chatServiceUserCreatedBinding(
            Queue chatServiceUserEventsQueue,
            TopicExchange userEventsExchange
    ) {
        return BindingBuilder
                .bind(chatServiceUserEventsQueue)
                .to(userEventsExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }
}