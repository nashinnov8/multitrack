package org.nashinnov8.multitrack.tracking.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.nashinnov8.multitrack.common.dto.PaginatedResponse;
import org.nashinnov8.multitrack.tracking.dto.request.ActivityLogRequest;
import org.nashinnov8.multitrack.tracking.dto.request.TrackCreateRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ActivityLogResponse;
import org.nashinnov8.multitrack.tracking.dto.response.TrackResponse;
import org.nashinnov8.multitrack.tracking.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

  private final TrackService trackService;

  public TrackController(TrackService trackService) {
    this.trackService = trackService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<TrackResponse>> createTrack(
      @Valid @RequestBody TrackCreateRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    TrackResponse track = trackService.createTrack(request, currentUserId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>("Track created successfully", track));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<TrackResponse>> getTrack(
      @PathVariable UUID id,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    TrackResponse track = trackService.getTrackById(id, currentUserId);
    return ResponseEntity.ok(new ApiResponse<>("Track retrieved successfully", track));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<List<TrackResponse>>> getUserTracks(
      @PathVariable UUID userId,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    List<TrackResponse> tracks = trackService.getAllTracksForUser(userId, currentUserId);
    return ResponseEntity.ok(new ApiResponse<>("User tracks retrieved successfully", tracks));
  }

  @GetMapping("/user/{userId}/paged")
  public ResponseEntity<ApiResponse<PaginatedResponse<TrackResponse>>> getUserTracksPaged(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    PaginatedResponse<TrackResponse> tracks = trackService.getAllTracksForUser(userId, page, size, currentUserId);
    return ResponseEntity.ok(new ApiResponse<>("User tracks retrieved successfully", tracks));
  }

  @PostMapping("/{id}/checkin")
  public ResponseEntity<ApiResponse<ActivityLogResponse>> checkIn(
      @PathVariable UUID id, 
      @Valid @RequestBody ActivityLogRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    ActivityLogResponse log = trackService.logActivity(id, request, currentUserId);
    return ResponseEntity.ok(new ApiResponse<>("Check-in successful", log));
  }

  @GetMapping("/stale")
  public ResponseEntity<ApiResponse<List<TrackResponse>>> getStaleTracks() {
    List<TrackResponse> tracks = trackService.findStaleTracks();
    return ResponseEntity.ok(new ApiResponse<>("Stale tracks retrieved successfully", tracks));
  }

  @GetMapping("/{id}/gaps")
  public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getTrackGaps(
      @PathVariable UUID id,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    List<ActivityLogResponse> gaps = trackService.getGaps(id, currentUserId);
    return ResponseEntity.ok(new ApiResponse<>("Gaps retrieved successfully", gaps));
  }

  @GetMapping("/{id}/gaps/paged")
  public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogResponse>>> getTrackGapsPaged(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @AuthenticationPrincipal Jwt jwt) {
    UUID currentUserId = UUID.fromString(jwt.getClaimAsString("userId"));
    PaginatedResponse<ActivityLogResponse> gaps = trackService.getGaps(id, page, size, currentUserId);
    return ResponseEntity.ok(new ApiResponse<>("Gaps retrieved successfully", gaps));
  }
}
