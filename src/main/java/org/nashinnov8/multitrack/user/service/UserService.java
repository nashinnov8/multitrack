package org.nashinnov8.multitrack.user.service;

import java.util.UUID;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.dto.request.UpdateUserRequest;
import org.nashinnov8.multitrack.user.dto.response.UserResponse;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setDisplayName(request.displayName());

        return UserResponse.from(userRepository.save(user));
    }
}
