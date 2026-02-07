package com.yappyd.userservice.dto.responce;

import java.util.Map;
import java.util.UUID;

public record BatchUsersResponse(
        Map<UUID, PublicUserProfileResponse> users
) {
}
