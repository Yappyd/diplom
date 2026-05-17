package com.yappyd.userservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthRabbitConfig {

    public static final String AUTH_EVENTS_EXCHANGE = "auth.events";

    public static final String AUTH_USER_CREATED_QUEUE = "user-service.auth-user-created.queue";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";

    @Bean
    public TopicExchange authEventsExchange() {
        return ExchangeBuilder
                .topicExchange(AUTH_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue authUserCreatedQueue() {
        return QueueBuilder
                .durable(AUTH_USER_CREATED_QUEUE)
                .build();
    }

    @Bean
    public Binding authUserCreatedBinding(
            Queue authUserCreatedQueue,
            TopicExchange authEventsExchange
    ) {
        return BindingBuilder
                .bind(authUserCreatedQueue)
                .to(authEventsExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }
}