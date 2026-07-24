package org.nashinnov8.multitrack.tracking.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Milestone;

public record MilestoneResponse(
        UUID id,
        UUID trackId,
        String name,
        String description,
        boolean isCompleted,
        Instant createdAt) {

    public static MilestoneResponse from(Milestone milestone) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getTrack().getId(),
                milestone.getName(),
                milestone.getDescription(),
                milestone.isCompleted(),
                milestone.getCreatedAt());
    }

    public static List<MilestoneResponse> fromList(List<Milestone> milestones) {
        return milestones.stream().map(MilestoneResponse::from).toList();
    }
}
