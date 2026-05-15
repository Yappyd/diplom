package com.yappyd.messageservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

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
    public Binding chatMessagePermissionBinding(Queue chatMessagePermissionQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder
                .bind(chatMessagePermissionQueue)
                .to(chatEventsExchange)
                .with(CHAT_MESSAGE_PERMISSION_ROUTING_PATTERN);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);

        return factory;
    }

}