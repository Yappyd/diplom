package com.yappyd.websocketservice.dto.websocket;

import com.yappyd.websocketservice.enums.WebSocketEventType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record WebSocketEvent<T>(
        UUID eventId,
        WebSocketEventType type,
        OffsetDateTime occurredAt,
        T payload
) {

    public static <T> WebSocketEvent<T> now(WebSocketEventType type, T payload) {
        return new WebSocketEvent<>(
                UUID.randomUUID(),
                type,
                OffsetDateTime.now(ZoneOffset.UTC),
                payload
        );
    }

    public static <T> WebSocketEvent<T> of(
            UUID eventId,
            WebSocketEventType type,
            OffsetDateTime occurredAt,
            T payload
    ) {
        return new WebSocketEvent<>(
                eventId,
                type,
                occurredAt,
                payload
        );
    }
}