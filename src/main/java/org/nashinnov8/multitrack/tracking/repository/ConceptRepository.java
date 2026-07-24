package org.nashinnov8.multitrack.tracking.repository;

import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Concept;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptRepository extends JpaRepository<Concept, UUID> {
  List<Concept> findByTrackId(UUID trackId);
}
