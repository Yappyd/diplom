package com.yappyd.websocketservice.security;

import java.security.Principal;
import java.util.UUID;

public record WebSocketUserPrincipal(UUID userId) implements Principal {

    @Override
    public String getName() {
        return userId.toString();
    }
}