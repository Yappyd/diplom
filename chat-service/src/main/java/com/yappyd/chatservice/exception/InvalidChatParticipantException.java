package com.yappyd.chatservice.exception;

public class InvalidChatParticipantException extends ChatServiceException {
    public InvalidChatParticipantException(String message) {
        super(ErrorCode.INVALID_CHAT_PARTICIPANT, message);
    }
}