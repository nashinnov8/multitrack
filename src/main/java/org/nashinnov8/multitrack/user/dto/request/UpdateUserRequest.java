package org.nashinnov8.multitrack.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Display name is required") String displayName,
        String timezone) {}
