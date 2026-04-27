package com.yappyd.chatservice.exception;

public abstract class ChatServiceException extends RuntimeException {
    private final ErrorCode errorCode;

    protected ChatServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ChatServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}