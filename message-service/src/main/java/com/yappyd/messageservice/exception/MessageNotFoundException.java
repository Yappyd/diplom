package com.yappyd.messageservice.exception;

import java.util.UUID;

public class MessageNotFoundException extends MessageServiceException {

    public MessageNotFoundException(UUID messageId) {
        super(ErrorCode.MESSAGE_NOT_FOUND, "Message not found: " + messageId);
    }
}