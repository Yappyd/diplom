package com.yappyd.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BatchUsersRequest(
        @NotEmpty(message = "USER_IDS_EMPTY")
        @Size(max = 100, message = "USER_IDS_TOO_MANY")
        List<@NotNull UUID> userIds
) {
}
