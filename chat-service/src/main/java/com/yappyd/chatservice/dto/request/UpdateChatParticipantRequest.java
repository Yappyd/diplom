package com.yappyd.chatservice.dto.request;

import com.yappyd.chatservice.enums.ParticipantRole;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UpdateChatParticipantRequest {

    private JsonNullable<ParticipantRole> role = JsonNullable.undefined();
    private JsonNullable<String> nickname = JsonNullable.undefined();
}