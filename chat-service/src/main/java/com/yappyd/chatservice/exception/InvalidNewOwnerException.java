package com.yappyd.chatservice.exception;

import java.util.UUID;

public class InvalidNewOwnerException extends ChatServiceException {

    public InvalidNewOwnerException(UUID chatId, UUID newOwnerId) {
        super(ErrorCode.INVALID_NEW_OWNER, "Invalid new owner " + newOwnerId + " for chat " + chatId);
    }
}