package com.yappyd.chatservice.exception;

public class PrivateChatWithSelfException extends ChatServiceException {
    public PrivateChatWithSelfException() {
        super(ErrorCode.PRIVATE_CHAT_WITH_SELF, "Cannot create private chat with yourself");
    }
}