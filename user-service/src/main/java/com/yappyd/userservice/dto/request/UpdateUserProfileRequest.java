package com.yappyd.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "FIRST_NAME_EMPTY")
        @Size(max = 64, message = "FIRST_NAME_TOO_LONG")
        String firstName,

        @Size(max = 64, message = "LAST_NAME_TOO_LONG")
        String lastName,

        @Size(min = 5, max = 64, message = "TAG_INVALID_LENGTH")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "TAG_INVALID_FORMAT"
        )
        String tag,

        boolean phoneIsVisible
) {
}