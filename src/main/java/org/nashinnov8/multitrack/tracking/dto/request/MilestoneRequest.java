package org.nashinnov8.multitrack.tracking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MilestoneRequest(
        @NotBlank(message = "Milestone name is required") String name,
        String description,
        boolean isCompleted) {}
