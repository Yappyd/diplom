package com.yappyd.messageservice.exception;

public class InvalidJwtException extends MessageServiceException {

    public InvalidJwtException(String message) {
        super(ErrorCode.INVALID_JWT, message);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(ErrorCode.INVALID_JWT, message, cause);
    }
}