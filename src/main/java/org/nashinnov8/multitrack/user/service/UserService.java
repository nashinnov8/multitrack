package org.nashinnov8.multitrack.user.service;

import java.util.UUID;
import org.nashinnov8.multitrack.common.exception.BusinessException;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.dto.request.UpdateUserRequest;
import org.nashinnov8.multitrack.user.dto.response.UserResponse;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.nashinnov8.multitrack.user.util.LevelCalculator;
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

        int calculatedLevel = LevelCalculator.calculateLevel(user.getTotalExp());
        int maxTrackStreak = user.getTracks().stream().mapToInt(Track::getCurrentStreak).max().orElse(0);
        int effectiveStreak = Math.max(user.getGlobalStreak(), maxTrackStreak);

        if (user.getLevel() != calculatedLevel || user.getGlobalStreak() != effectiveStreak) {
            user.setLevel(calculatedLevel);
            user.setGlobalStreak(effectiveStreak);
            user = userRepository.save(user);
        }

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setDisplayName(request.displayName());
        if (request.timezone() != null && !request.timezone().isBlank()) {
            user.setTimezone(request.timezone());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse buyStreakFreeze(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getTotalExp() < 500) {
            throw new BusinessException("Cần tối thiểu 500 EXP để mua 1 Khiên Bảo Vệ Streak (Bạn hiện có " + user.getTotalExp() + " EXP)");
        }

        user.setTotalExp(user.getTotalExp() - 500);
        user.setStreakFreezeCount(user.getStreakFreezeCount() + 1);
        return UserResponse.from(userRepository.save(user));
    }
}
