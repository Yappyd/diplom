package com.yappyd.messageservice.exception;

public abstract class MessageServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    protected MessageServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected MessageServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}