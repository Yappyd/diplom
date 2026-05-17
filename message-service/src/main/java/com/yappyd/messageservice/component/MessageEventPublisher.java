package com.yappyd.messageservice.component;

import com.yappyd.messageservice.dto.event.MessageCreatedEvent;
import com.yappyd.messageservice.dto.event.MessageDeletedEvent;
import com.yappyd.messageservice.dto.event.MessageUpdatedEvent;
import com.yappyd.messageservice.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishMessageCreated(Message message) {
        applicationEventPublisher.publishEvent(new MessageCreatedEvent(
                message.getMessageId(),
                message.getChatId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt()
        ));
    }

    public void publishMessageUpdated(Message message) {
        applicationEventPublisher.publishEvent(new MessageUpdatedEvent(
                message.getMessageId(),
                message.getChatId(),
                message.getSenderId(),
                message.getContent(),
                message.getUpdatedAt()
        ));
    }

    public void publishMessageDeleted(Message message) {
        applicationEventPublisher.publishEvent(new MessageDeletedEvent(
                message.getMessageId(),
                message.getChatId(),
                message.getDeletedAt()
        ));
    }
}