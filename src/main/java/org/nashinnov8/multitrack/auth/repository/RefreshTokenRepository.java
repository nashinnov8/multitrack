package org.nashinnov8.multitrack.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.nashinnov8.multitrack.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);
}
