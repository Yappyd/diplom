package com.yappyd.chatservice.exception;

import java.util.UUID;

public class ParticipantAlreadyExistsException extends ChatServiceException {

    public ParticipantAlreadyExistsException(UUID chatId, UUID userId) {
        super(ErrorCode.PARTICIPANT_ALREADY_EXISTS, "User " + userId + " is already participant of chat " + chatId);
    }
}