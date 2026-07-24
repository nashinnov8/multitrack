package org.nashinnov8.multitrack.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackRepository extends JpaRepository<Track, UUID> {
    List<Track> findByUserId(UUID userId);

    Page<Track> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT t FROM Track t JOIN FETCH t.user WHERE t.id = :id")
    Optional<Track> findByIdWithUser(@Param("id") UUID id);

    @Query(value = "SELECT * FROM tracks " +
                   "WHERE status = 'ACTIVE' " +
                   "AND last_activity_at < (NOW() - (inactivity_threshold_days * INTERVAL '1 day'))",
           nativeQuery = true)
    List<Track> findOverdueTracks();
}
