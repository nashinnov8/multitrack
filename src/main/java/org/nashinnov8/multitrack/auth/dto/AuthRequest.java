package org.nashinnov8.multitrack.auth.dto;

public record AuthRequest(
    String email,
    String password
) {}
