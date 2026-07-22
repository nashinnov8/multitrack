package org.nashinnov8.multitrack.auth.dto;

public record AuthResponse(
    String token,
    String username
) {}
