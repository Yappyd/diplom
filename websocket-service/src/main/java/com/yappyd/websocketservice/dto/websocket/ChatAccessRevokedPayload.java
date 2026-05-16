package com.yappyd.websocketservice.dto.websocket;

import java.util.UUID;

public record ChatAccessRevokedPayload(
        UUID chatId
) {
}