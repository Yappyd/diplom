package com.yappyd.userservice.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRabbitConfig {

    public static final String USER_EVENTS_EXCHANGE = "user.events";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";

    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder
                .topicExchange(USER_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }
}