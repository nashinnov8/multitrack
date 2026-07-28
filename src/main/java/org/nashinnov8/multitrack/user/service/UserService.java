package org.nashinnov8.multitrack.user.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.nashinnov8.multitrack.common.exception.BusinessException;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.ActivityLog;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.repository.ActivityLogRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.dto.request.UpdateUserRequest;
import org.nashinnov8.multitrack.user.dto.response.ActivityHeatmapDayResponse;
import org.nashinnov8.multitrack.user.dto.response.UserResponse;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.nashinnov8.multitrack.user.util.LevelCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    public UserService(UserRepository userRepository, ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
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

    public List<ActivityHeatmapDayResponse> getActivityHeatmap(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ZoneId userZone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Ho_Chi_Minh");
        Instant startDate = Instant.now().minus(365, ChronoUnit.DAYS);

        List<ActivityLog> logs = activityLogRepository.findByTrackUserIdAndCreatedAtGreaterThanEqual(userId, startDate);

        Map<String, Integer> countsByDate = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ActivityLog log : logs) {
            String dateStr = log.getCreatedAt().atZone(userZone).toLocalDate().format(formatter);
            countsByDate.put(dateStr, countsByDate.getOrDefault(dateStr, 0) + 1);
        }

        List<ActivityHeatmapDayResponse> result = new ArrayList<>();
        countsByDate.forEach((date, count) -> result.add(new ActivityHeatmapDayResponse(date, count)));
        return result;
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
