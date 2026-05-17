package com.yappyd.chatservice.exception;

import java.util.UUID;

public class UnknownUserException extends RuntimeException {

    public UnknownUserException(UUID userId) {
        super("User is not known by chat-service: " + userId);
    }
}