package org.nashinnov8.multitrack.gamification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record BadgeRequest(
        @NotBlank(message = "Badge name is required") String name,
        String description,
        String iconUrl,
        @PositiveOrZero(message = "EXP reward must be zero or positive") int expReward) {}
