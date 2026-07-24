package org.nashinnov8.multitrack.gamification.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.gamification.domain.UserBadge;

public record UserBadgeResponse(
        UUID id,
        UUID badgeId,
        String badgeName,
        String iconUrl,
        int expReward,
        Instant earnedAt) {

    public static UserBadgeResponse from(UserBadge userBadge) {
        return new UserBadgeResponse(
                userBadge.getId(),
                userBadge.getBadge().getId(),
                userBadge.getBadge().getName(),
                userBadge.getBadge().getIconUrl(),
                userBadge.getBadge().getExpReward(),
                userBadge.getEarnedAt());
    }

    public static List<UserBadgeResponse> fromList(List<UserBadge> userBadges) {
        return userBadges.stream().map(UserBadgeResponse::from).toList();
    }
}
