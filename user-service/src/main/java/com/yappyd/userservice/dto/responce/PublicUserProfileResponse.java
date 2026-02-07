package com.yappyd.userservice.dto.responce;

import java.util.UUID;

public record PublicUserProfileResponse(
        UUID userId,
        String PhoneNumber,
        String tag,
        String firstName,
        String lastName
) {
}
