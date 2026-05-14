package com.yappyd.messageservice.exception;

import java.util.UUID;

public class ChatNotFoundException extends MessageServiceException {

    public ChatNotFoundException(UUID chatId) {
        super(ErrorCode.CHAT_NOT_FOUND, "Chat not found: " + chatId);
    }
}