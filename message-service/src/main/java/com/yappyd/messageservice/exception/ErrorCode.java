package com.yappyd.messageservice.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_MESSAGE("INVALID_MESSAGE", HttpStatus.BAD_REQUEST),

    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", HttpStatus.NOT_FOUND),
    MESSAGE_ACCESS_DENIED("MESSAGE_ACCESS_DENIED", HttpStatus.FORBIDDEN),
    MESSAGE_ALREADY_DELETED("MESSAGE_ALREADY_DELETED", HttpStatus.CONFLICT),

    CHAT_NOT_FOUND("CHAT_NOT_FOUND", HttpStatus.NOT_FOUND),
    CHAT_ACCESS_DENIED("CHAT_ACCESS_DENIED", HttpStatus.FORBIDDEN),

    INVALID_JWT("INVALID_JWT", HttpStatus.UNAUTHORIZED),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_PARAMETER("INVALID_REQUEST_PARAMETER", HttpStatus.BAD_REQUEST),

    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION", HttpStatus.CONFLICT),

    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}