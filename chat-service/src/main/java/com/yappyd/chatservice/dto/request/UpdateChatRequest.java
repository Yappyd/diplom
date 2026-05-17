package com.yappyd.chatservice.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UpdateChatRequest {

    private JsonNullable<String> title = JsonNullable.undefined();
}