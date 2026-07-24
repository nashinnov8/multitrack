package org.nashinnov8.multitrack.tracking.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ActivityLogResponse(
    UUID id,
    UUID trackId,
    String note,
    String whatLearned,
    String explainSimply,
    String gapsFound,
    UUID conceptId,
    Instant createdAt) {
  public static ActivityLogResponse from(
      org.nashinnov8.multitrack.tracking.domain.ActivityLog log) {
    return new ActivityLogResponse(
        log.getId(),
        log.getTrack().getId(),
        log.getNote(),
        log.getWhatLearned(),
        log.getExplainSimply(),
        log.getGapsFound(),
        log.getConcept() != null ? log.getConcept().getId() : null,
        log.getCreatedAt());
  }
}
