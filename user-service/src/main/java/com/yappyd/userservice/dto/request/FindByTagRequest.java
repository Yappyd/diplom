package com.yappyd.userservice.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FindByTagRequest(
        @Size(min = 5, max = 64, message = "TAG_INVALID_LENGTH")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "TAG_INVALID_FORMAT"
        )
        String tag
) {
}
