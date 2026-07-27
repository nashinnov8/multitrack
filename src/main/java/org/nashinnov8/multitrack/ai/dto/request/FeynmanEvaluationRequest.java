package org.nashinnov8.multitrack.ai.dto.request;

public record FeynmanEvaluationRequest(
    String conceptName,
    String explainSimply,
    String whatLearned,
    String note
) {}
