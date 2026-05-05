package com.yappyd.chatservice.dto.response;

import java.util.List;

public record ChatListResponse(
        List<ChatResponse> chats
) {
}