package org.nashinnov8.multitrack.tracking.repository;

import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, UUID> {}
