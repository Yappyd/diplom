package com.yappyd.messageservice.exception;

import java.util.UUID;

public class MessageAccessDeniedException extends MessageServiceException {

    public MessageAccessDeniedException(UUID messageId) {
        super(ErrorCode.MESSAGE_ACCESS_DENIED, "Access denied to message: " + messageId);
    }
}