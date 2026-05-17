package com.yappyd.messageservice.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketRabbitConfig {

    public static final String MESSAGE_EVENTS_EXCHANGE = "message.events";

    public static final String MESSAGE_CREATED_ROUTING_KEY = "message.created";
    public static final String MESSAGE_UPDATED_ROUTING_KEY = "message.updated";
    public static final String MESSAGE_DELETED_ROUTING_KEY = "message.deleted";

    @Bean
    public TopicExchange messageEventsExchange() {
        return ExchangeBuilder
                .topicExchange(MESSAGE_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }
}