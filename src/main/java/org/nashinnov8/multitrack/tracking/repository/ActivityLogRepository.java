package org.nashinnov8.multitrack.tracking.repository;

import java.util.UUID;

import org.nashinnov8.multitrack.tracking.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    @Query("SELECT a FROM ActivityLog a WHERE a.track.id = :trackId AND a.gapsFound IS NOT NULL AND a.gapsFound <> '' ORDER BY a.createdAt DESC")
    List<ActivityLog> findGapsByTrackId(@Param("trackId") UUID trackId);
}
