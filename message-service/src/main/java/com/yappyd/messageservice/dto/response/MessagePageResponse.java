package com.yappyd.messageservice.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record MessagePageResponse(
        List<MessageResponse> items,
        int limit,
        boolean hasMore,
        OffsetDateTime nextBefore
) {
}