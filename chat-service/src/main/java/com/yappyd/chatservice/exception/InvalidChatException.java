package com.yappyd.chatservice.exception;

public class InvalidChatException extends ChatServiceException {
    public InvalidChatException(String message) {
        super(ErrorCode.INVALID_CHAT, message);
    }
}