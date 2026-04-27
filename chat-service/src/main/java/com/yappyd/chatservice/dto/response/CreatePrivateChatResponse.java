package com.yappyd.chatservice.dto.response;

import java.util.UUID;

public record CreatePrivateChatResponse(
        UUID chatId
) {
}
