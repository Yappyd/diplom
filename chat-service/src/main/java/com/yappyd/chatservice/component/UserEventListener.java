package com.yappyd.chatservice.component;

import com.yappyd.chatservice.config.UserRabbitConfig;
import com.yappyd.chatservice.dto.event.UserCreatedEvent;
import com.yappyd.chatservice.service.KnownUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final KnownUserService knownUserService;

    @RabbitListener(queues = UserRabbitConfig.CHAT_SERVICE_USER_EVENTS_QUEUE)
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user.created event: {}", event);

        knownUserService.upsertUser(event.userId());
    }
}