package com.yappyd.messageservice.exception;

public class InvalidMessageException extends MessageServiceException {

    public InvalidMessageException(String message) {
        super(ErrorCode.INVALID_MESSAGE, message);
    }
}