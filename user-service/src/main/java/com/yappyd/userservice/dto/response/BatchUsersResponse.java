package com.yappyd.userservice.dto.response;

import java.util.Map;
import java.util.UUID;

public record BatchUsersResponse(
        Map<UUID, PublicUserProfileResponse> users
) {
}
