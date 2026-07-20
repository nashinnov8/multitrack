package org.nashinnov8.multitrack.tracking.repository;

import java.util.UUID;

import org.nashinnov8.multitrack.tracking.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

}
