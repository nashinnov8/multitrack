package org.nashinnov8.multitrack.tracking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrackCreateRequest(
    @NotBlank(message = "Track name is required") String name,
    String description,
    boolean isPublic) {}
