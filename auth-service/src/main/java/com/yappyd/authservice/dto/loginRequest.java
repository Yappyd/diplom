package com.yappyd.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record loginRequest(
        @NotBlank(message = "PHONE_EMPTY")
        @Pattern(
                regexp = "^\\+7\\d{10}$",
                message = "PHONE_INVALID_FORMAT"
        )
        String phoneNumber
) {
}