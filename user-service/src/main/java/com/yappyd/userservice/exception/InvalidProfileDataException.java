package com.yappyd.userservice.exception;

public class InvalidProfileDataException extends RuntimeException {
    public InvalidProfileDataException(String message) {
        super(message);
    }
}
