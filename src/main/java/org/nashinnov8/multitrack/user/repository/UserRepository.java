package org.nashinnov8.multitrack.user.repository;

import java.util.UUID;

import org.nashinnov8.multitrack.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>{

}
