package com.yappyd.messageservice.exception;

import java.util.UUID;

public class MessageAlreadyDeletedException extends MessageServiceException {

    public MessageAlreadyDeletedException(UUID messageId) {
        super(ErrorCode.MESSAGE_ALREADY_DELETED, "Message is already deleted: " + messageId);
    }
}