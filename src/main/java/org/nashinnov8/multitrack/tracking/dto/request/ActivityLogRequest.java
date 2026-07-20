package org.nashinnov8.multitrack.tracking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ActivityLogRequest(
        @NotBlank(message = "Note cannot be empty")
        String note
) {}
