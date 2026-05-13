package com.yappyd.chatservice.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_CHAT("INVALID_CHAT", HttpStatus.BAD_REQUEST),
    INVALID_PRIVATE_CHAT("INVALID_PRIVATE_CHAT", HttpStatus.BAD_REQUEST),
    PRIVATE_CHAT_WITH_SELF("PRIVATE_CHAT_WITH_SELF", HttpStatus.BAD_REQUEST),
    PRIVATE_CHAT_TITLE_UPDATE_NOT_ALLOWED("PRIVATE_CHAT_TITLE_UPDATE_NOT_ALLOWED", HttpStatus.BAD_REQUEST),
    CHAT_NOT_FOUND("CHAT_NOT_FOUND", HttpStatus.NOT_FOUND),
    CHAT_ACCESS_DENIED("CHAT_ACCESS_DENIED", HttpStatus.FORBIDDEN),

    INVALID_CHAT_PARTICIPANT("INVALID_CHAT_PARTICIPANT", HttpStatus.BAD_REQUEST),
    PARTICIPANT_ALREADY_EXISTS("PARTICIPANT_ALREADY_EXISTS", HttpStatus.CONFLICT),
    PARTICIPANT_NOT_FOUND("PARTICIPANT_NOT_FOUND", HttpStatus.NOT_FOUND),
    NEW_OWNER_REQUIRED("NEW_OWNER_REQUIRED", HttpStatus.BAD_REQUEST),
    INVALID_NEW_OWNER("INVALID_NEW_OWNER", HttpStatus.BAD_REQUEST),

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