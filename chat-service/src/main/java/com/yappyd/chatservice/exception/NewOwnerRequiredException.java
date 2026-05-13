package com.yappyd.chatservice.exception;

import java.util.UUID;

public class NewOwnerRequiredException extends ChatServiceException {

    public NewOwnerRequiredException(UUID chatId) {
        super(ErrorCode.NEW_OWNER_REQUIRED, "New owner is required when owner leaves chat " + chatId);
    }
}