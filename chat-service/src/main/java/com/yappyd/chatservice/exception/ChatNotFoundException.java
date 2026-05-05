package com.yappyd.chatservice.exception;

import java.util.UUID;

public class ChatNotFoundException extends ChatServiceException {
    public ChatNotFoundException(UUID chatId) {
        super(ErrorCode.CHAT_NOT_FOUND, "Chat not found: " + chatId);
    }
}
