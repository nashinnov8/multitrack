package org.nashinnov8.multitrack.gamification.repository;

import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.gamification.domain.UserBadge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {
    List<UserBadge> findByUserId(UUID userId);

    Page<UserBadge> findByUserId(UUID userId, Pageable pageable);
}
