package org.nashinnov8.multitrack.ai.dto.response;

public record FeynmanEvaluationResponse(
    int score,
    String feedback,
    String jargonWarning,
    String suggestedGap
) {}
