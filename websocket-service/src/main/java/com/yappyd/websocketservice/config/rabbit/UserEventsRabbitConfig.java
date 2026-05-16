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
public class UserEventsRabbitConfig {

    public static final String USER_EVENTS_EXCHANGE = "user.events";
    public static final String USER_EVENTS_QUEUE = "websocket-service.user-events.queue";
    public static final String USER_PROFILE_UPDATED_ROUTING_KEY = "user.profile.updated";

    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder
                .topicExchange(USER_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue userEventsQueue() {
        return QueueBuilder
                .durable(USER_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Binding userProfileUpdatedBinding(Queue userEventsQueue, TopicExchange userEventsExchange) {
        return BindingBuilder
                .bind(userEventsQueue)
                .to(userEventsExchange)
                .with(USER_PROFILE_UPDATED_ROUTING_KEY);
    }
}