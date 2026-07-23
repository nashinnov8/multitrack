package org.nashinnov8.multitrack.tracking.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ActivityLogResponse(
        UUID id,
        UUID trackId,
        String note,
        Instant createdAt
) {}
