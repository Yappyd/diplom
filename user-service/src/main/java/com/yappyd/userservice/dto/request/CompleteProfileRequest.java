package com.yappyd.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteProfileRequest(
        @NotBlank(message = "FIRST_NAME_EMPTY")
        @Size(max = 64, message = "FIRST_NAME_TOO_LONG")
        String firstName
) {
}
