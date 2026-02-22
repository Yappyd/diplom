package com.yappyd.userservice.dto.request;

import jakarta.validation.constraints.Pattern;

public record FindByPhoneRequest(
        @Pattern(
                regexp = "^\\+7\\d{10}$",
                message = "PHONE_INVALID_FORMAT"
        )
        String phoneNumber
) {
}