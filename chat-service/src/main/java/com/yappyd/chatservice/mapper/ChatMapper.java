package com.yappyd.chatservice.mapper;

import com.yappyd.chatservice.dto.response.ChatParticipantResponse;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.model.PrivateChat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ChatMapper {

    public ChatResponse toChatResponse(Chat chat, PrivateChat privateChat) {
        if (privateChat == null) {
            throw new IllegalStateException("PrivateChat not found for chatId: " + chat.getId());
        }
        return toChatResponse(chat, List.of(privateChat.getUserA(), privateChat.getUserB())
        );
    }

    public ChatResponse toChatResponse(Chat chat, List<UUID> participantIds) {
        return new ChatResponse(
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                chat.getCreatedBy(),
                chat.getCreatedAt(),
                chat.getUpdatedAt(),
                participantIds
        );
    }

    public ChatParticipantResponse toChatParticipantResponse(ChatParticipant participant) {
        return new ChatParticipantResponse(
                participant.getId().getUserId(),
                participant.getRole(),
                participant.getNickname(),
                participant.getJoinedAt(),
                participant.getUpdatedAt()
        );
    }
}