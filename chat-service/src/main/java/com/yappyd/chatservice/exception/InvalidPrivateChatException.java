package com.yappyd.chatservice.exception;

public class InvalidPrivateChatException extends ChatServiceException {
    public InvalidPrivateChatException(String message) {
        super(ErrorCode.INVALID_PRIVATE_CHAT, message);
    }
}
