package com.yappyd.authservice.exception;

public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException(String message) {
        super(message);
    }

    public RefreshTokenException(String message, Exception e) {
        super(message, e);
    }
}
