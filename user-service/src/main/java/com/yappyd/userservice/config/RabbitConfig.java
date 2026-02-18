package com.yappyd.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    public static final String AUTH_EVENTS_EXCHANGE = "auth.events";
    public static final String USER_CREATED_QUEUE = "user.profile.creation.queue";
    public static final String ROUTING_KEY = "user.created";

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder
                .durable(USER_CREATED_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange authEventsExchange() {
        return ExchangeBuilder
                .topicExchange(AUTH_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange authEventsExchange) {
        return BindingBuilder
                .bind(userCreatedQueue)
                .to(authEventsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jsonConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, JacksonJsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);

        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);

        return factory;
    }

}
