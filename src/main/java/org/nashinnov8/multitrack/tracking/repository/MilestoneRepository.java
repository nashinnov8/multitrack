package org.nashinnov8.multitrack.tracking.repository;

import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {}
