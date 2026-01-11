package com.yappyd.authservice.exception;

public class KeyInitializationException extends RuntimeException {
    public KeyInitializationException(String message, Exception e) {
        super(message, e);
    }
}
