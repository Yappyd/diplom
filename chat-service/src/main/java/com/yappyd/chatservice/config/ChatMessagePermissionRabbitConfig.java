package com.yappyd.chatservice.config;

public final class ChatMessagePermissionRabbitConfig {

    public static final String CHAT_MESSAGE_PERMISSION_UPSERTED_ROUTING_KEY = "chat.message-permission.upserted";
    public static final String CHAT_MESSAGE_PERMISSION_DELETED_ROUTING_KEY = "chat.message-permission.deleted";
    private ChatMessagePermissionRabbitConfig() {}
}