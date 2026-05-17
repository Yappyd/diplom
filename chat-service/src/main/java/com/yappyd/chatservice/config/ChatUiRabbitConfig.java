package com.yappyd.chatservice.config;

public final class ChatUiRabbitConfig {

    public static final String CHAT_CREATED_ROUTING_KEY = "chat.created";
    public static final String CHAT_UPDATED_ROUTING_KEY = "chat.updated";
    public static final String CHAT_DELETED_ROUTING_KEY = "chat.deleted";
    public static final String CHAT_PARTICIPANT_ADDED_ROUTING_KEY = "chat.participant.added";
    public static final String CHAT_PARTICIPANT_UPDATED_ROUTING_KEY = "chat.participant.updated";
    public static final String CHAT_PARTICIPANT_REMOVED_ROUTING_KEY = "chat.participant.removed";

    private ChatUiRabbitConfig() {}
}