package com.yappyd.chatservice.exception;

import java.util.UUID;

public class ParticipantNotFoundException extends ChatServiceException {

    public ParticipantNotFoundException(UUID chatId, UUID userId) {
        super(ErrorCode.PARTICIPANT_NOT_FOUND, "User " + userId + " is not participant of chat " + chatId);
    }
}