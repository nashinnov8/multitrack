package org.nashinnov8.multitrack.user.dto.response;

import java.util.UUID;
import org.nashinnov8.multitrack.user.domain.User;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        int totalExp,
        int level,
        int globalStreak,
        String timezone) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getTotalExp(),
                user.getLevel(),
                user.getGlobalStreak(),
                user.getTimezone());
    }
}
