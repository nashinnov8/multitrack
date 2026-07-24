package org.nashinnov8.multitrack.gamification.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.gamification.domain.Badge;
import org.nashinnov8.multitrack.gamification.domain.UserBadge;
import org.nashinnov8.multitrack.gamification.dto.request.BadgeRequest;
import org.nashinnov8.multitrack.gamification.dto.response.BadgeResponse;
import org.nashinnov8.multitrack.gamification.dto.response.UserBadgeResponse;
import org.nashinnov8.multitrack.gamification.repository.BadgeRepository;
import org.nashinnov8.multitrack.gamification.repository.UserBadgeRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    public BadgeService(
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            UserRepository userRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userRepository = userRepository;
    }

    public List<BadgeResponse> getAllBadges() {
        return BadgeResponse.fromList(badgeRepository.findAll());
    }

    @Transactional
    public BadgeResponse createBadge(BadgeRequest request) {
        Badge badge = Badge.builder()
                .name(request.name())
                .description(request.description())
                .iconUrl(request.iconUrl())
                .expReward(request.expReward())
                .build();

        return BadgeResponse.from(badgeRepository.save(badge));
    }

    @Transactional
    public UserBadgeResponse awardBadgeToUser(UUID badgeId, UUID userId) {
        Badge badge = badgeRepository
                .findById(badgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with id: " + badgeId));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserBadge userBadge = UserBadge.builder()
                .badge(badge)
                .user(user)
                .earnedAt(Instant.now())
                .build();

        // Add badge EXP reward to user total
        user.setTotalExp(user.getTotalExp() + badge.getExpReward());
        userRepository.save(user);

        return UserBadgeResponse.from(userBadgeRepository.save(userBadge));
    }

    public List<UserBadgeResponse> getBadgesForUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return UserBadgeResponse.fromList(userBadgeRepository.findByUserId(userId));
    }
}
