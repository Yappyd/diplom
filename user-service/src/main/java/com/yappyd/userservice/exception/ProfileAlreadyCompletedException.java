package com.yappyd.userservice.exception;

public class ProfileAlreadyCompletedException extends RuntimeException {
    public ProfileAlreadyCompletedException() {
        super("Profile is already completed");
    }
}
