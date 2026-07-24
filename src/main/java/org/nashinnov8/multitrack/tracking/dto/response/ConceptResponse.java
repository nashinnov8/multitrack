package org.nashinnov8.multitrack.tracking.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Concept;

public record ConceptResponse(
        UUID id,
        UUID trackId,
        String name,
        String status,
        Instant createdAt) {

    public static ConceptResponse from(Concept concept) {
        return new ConceptResponse(
                concept.getId(),
                concept.getTrack().getId(),
                concept.getName(),
                concept.getStatus().name(),
                concept.getCreatedAt());
    }

    public static List<ConceptResponse> fromList(List<Concept> concepts) {
        return concepts.stream().map(ConceptResponse::from).toList();
    }
}
