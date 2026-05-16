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
public class MessageEventsRabbitConfig {

    public static final String MESSAGE_EVENTS_EXCHANGE = "message.events";
    public static final String MESSAGE_EVENTS_QUEUE = "websocket-service.message-events.queue";
    public static final String MESSAGE_EVENTS_ROUTING_PATTERN = "message.*";
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

    @Bean
    public Queue messageEventsQueue() {
        return QueueBuilder
                .durable(MESSAGE_EVENTS_QUEUE)
                .build();
    }

    @Bean
    public Binding messageEventsBinding(Queue messageEventsQueue, TopicExchange messageEventsExchange) {
        return BindingBuilder
                .bind(messageEventsQueue)
                .to(messageEventsExchange)
                .with(MESSAGE_EVENTS_ROUTING_PATTERN);
    }
}