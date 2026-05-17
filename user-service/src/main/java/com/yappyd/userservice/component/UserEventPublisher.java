package com.yappyd.userservice.component;

import com.yappyd.userservice.dto.event.UserProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishUserCreated(UUID userId) {
        applicationEventPublisher.publishEvent(new UserProfileCreatedEvent(userId));
    }
}