package com.yappyd.messageservice.exception;

import java.util.UUID;

public class ChatAccessDeniedException extends MessageServiceException {

  public ChatAccessDeniedException(UUID chatId) {
    super(ErrorCode.CHAT_ACCESS_DENIED, "Access denied to chat: " + chatId);
  }
}