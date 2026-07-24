package org.nashinnov8.multitrack.tracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.nashinnov8.multitrack.tracking.domain.ConceptStatus;

public record ConceptRequest(
        @NotBlank(message = "Concept name is required") String name,
        ConceptStatus status) {}
