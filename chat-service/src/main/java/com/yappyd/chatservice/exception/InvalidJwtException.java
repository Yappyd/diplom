package com.yappyd.chatservice.exception;

public class InvalidJwtException extends ChatServiceException {
    public InvalidJwtException(String message) {
        super(ErrorCode.INVALID_JWT, message);
    }
    public InvalidJwtException(String message, Throwable cause) {
        super(ErrorCode.INVALID_JWT, message, cause);
    }
}