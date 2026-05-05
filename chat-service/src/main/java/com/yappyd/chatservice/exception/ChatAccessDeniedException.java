package com.yappyd.chatservice.exception;

import java.util.UUID;

public class ChatAccessDeniedException extends ChatServiceException {

    public ChatAccessDeniedException(UUID chatId) {
        super(ErrorCode.CHAT_ACCESS_DENIED, "Access denied to chat: " + chatId);
    }
}