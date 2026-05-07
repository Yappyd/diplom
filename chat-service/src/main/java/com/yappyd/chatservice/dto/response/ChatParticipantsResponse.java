package com.yappyd.chatservice.dto.response;

import java.util.List;

public record ChatParticipantsResponse(
        List<ChatParticipantResponse> participants
) {
}