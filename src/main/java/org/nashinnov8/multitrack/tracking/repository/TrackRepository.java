package org.nashinnov8.multitrack.tracking.repository;

import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrackRepository extends JpaRepository<Track, UUID> {
    List<Track> findByUserId(UUID userId);

    @Query(value = "SELECT * FROM tracks " +
                   "WHERE status = 'ACTIVE' " +
                   "AND last_activity_at < (NOW() - (inactivity_threshold_days * INTERVAL '1 day'))", 
           nativeQuery = true)
    List<Track> findOverdueTracks();
}
