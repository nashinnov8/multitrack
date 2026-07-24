package org.nashinnov8.multitrack.tracking.repository;

import org.nashinnov8.multitrack.tracking.domain.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ConceptRepository extends JpaRepository<Concept, UUID> {
    List<Concept> findByTrackId(UUID trackId);
}
