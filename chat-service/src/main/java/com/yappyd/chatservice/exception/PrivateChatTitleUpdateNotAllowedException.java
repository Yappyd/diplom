package com.yappyd.chatservice.exception;

public class PrivateChatTitleUpdateNotAllowedException extends ChatServiceException {
    public PrivateChatTitleUpdateNotAllowedException() {
        super(ErrorCode.PRIVATE_CHAT_TITLE_UPDATE_NOT_ALLOWED, "Private chat title cannot be updated");
    }
}