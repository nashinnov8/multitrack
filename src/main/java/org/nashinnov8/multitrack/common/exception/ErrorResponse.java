package org.nashinnov8.multitrack.common.exception;

import java.time.Instant;

public record ErrorResponse(
    int status,
    String error,
    String message,
    Instant timestamp
) {

}
