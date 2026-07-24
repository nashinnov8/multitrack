package org.nashinnov8.multitrack.gamification.repository;

import java.util.UUID;
import org.nashinnov8.multitrack.gamification.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {}
