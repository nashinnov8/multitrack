package org.nashinnov8.multitrack.tracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TrackCreateRequest(
    @NotNull(message = "User ID is required") UUID userId,
    @NotBlank(message = "Track name is required") String name,
    String description,
    boolean isPublic) {}
