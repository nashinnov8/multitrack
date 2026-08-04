package org.nashinnov8.multitrack.tracking.dto.request;

public record ActivityLogRequest(
    String note,
    String whatLearned,
    String explainSimply,
    String gapsFound,
    java.util.UUID conceptId) {}
