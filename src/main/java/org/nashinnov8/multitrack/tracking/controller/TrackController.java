package org.nashinnov8.multitrack.tracking.controller;

import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.nashinnov8.multitrack.tracking.dto.request.ActivityLogRequest;
import org.nashinnov8.multitrack.tracking.dto.request.TrackCreateRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ActivityLogResponse;
import org.nashinnov8.multitrack.tracking.dto.response.TrackResponse;
import org.nashinnov8.multitrack.tracking.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrackResponse>> createTrack(@Valid @RequestBody TrackCreateRequest request) {
        TrackResponse track = trackService.createTrack(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Track created successfully", track));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrackResponse>> getTrack(@PathVariable UUID id) {
        TrackResponse track = trackService.getTrackById(id);
        return ResponseEntity.ok(new ApiResponse<>("Track retrieved successfully", track));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TrackResponse>>> getUserTracks(@PathVariable UUID userId) {
        List<TrackResponse> tracks = trackService.getAllTracksForUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("User tracks retrieved successfully", tracks));
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<ApiResponse<ActivityLogResponse>> checkIn(
            @PathVariable UUID id, 
            @Valid @RequestBody ActivityLogRequest request) {
        ActivityLogResponse log = trackService.logActivity(id, request);
        return ResponseEntity.ok(new ApiResponse<>("Check-in successful", log));
    }

    @GetMapping("/stale")
    public ResponseEntity<ApiResponse<List<TrackResponse>>> getStaleTracks() {
        List<TrackResponse> tracks = trackService.findStaleTracks();
        return ResponseEntity.ok(new ApiResponse<>("Stale tracks retrieved successfully", tracks));
    }
}
