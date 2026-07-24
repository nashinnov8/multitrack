package org.nashinnov8.multitrack.gamification.repository;

import java.util.UUID;
import org.nashinnov8.multitrack.gamification.domain.Badge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    Page<Badge> findAll(Pageable pageable);
}
