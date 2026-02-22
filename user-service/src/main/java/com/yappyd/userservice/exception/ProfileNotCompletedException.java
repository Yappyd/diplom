package com.yappyd.userservice.exception;

public class ProfileNotCompletedException extends RuntimeException {
    public ProfileNotCompletedException() {
        super("Profile is not completed yet");
    }
}
