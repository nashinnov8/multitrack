package org.nashinnov8.multitrack.tracking.dto.response;

import java.time.Instant;
import java.util.UUID;
import org.nashinnov8.multitrack.tracking.domain.Track;

public record TrackResponse(
    UUID id,
    UUID userId,
    String name,
    String description,
    String slug,
    boolean isPublic,
    Instant lastActivityAt,
    int currentStreak,
    int longestStreak,
    String status) {
  public static TrackResponse from(Track track) {
    return new TrackResponse(
        track.getId(),
        track.getUser().getId(),
        track.getName(),
        track.getDescription(),
        track.getSlug(),
        track.isPublic(),
        track.getLastActivityAt(),
        track.getCurrentStreak(),
        track.getLongestStreak(),
        track.getStatus().name());
  }
}
