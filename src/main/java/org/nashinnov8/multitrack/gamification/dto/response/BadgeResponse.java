package org.nashinnov8.multitrack.gamification.dto.response;

import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.gamification.domain.Badge;

public record BadgeResponse(
        UUID id,
        String name,
        String description,
        String iconUrl,
        int expReward) {

    public static BadgeResponse from(Badge badge) {
        return new BadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getExpReward());
    }

    public static List<BadgeResponse> fromList(List<Badge> badges) {
        return badges.stream().map(BadgeResponse::from).toList();
    }
}
