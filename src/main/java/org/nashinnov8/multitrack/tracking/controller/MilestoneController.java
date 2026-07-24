package org.nashinnov8.multitrack.tracking.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.nashinnov8.multitrack.tracking.dto.request.MilestoneRequest;
import org.nashinnov8.multitrack.tracking.dto.response.MilestoneResponse;
import org.nashinnov8.multitrack.tracking.service.MilestoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tracks/{trackId}/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MilestoneResponse>> createMilestone(
            @PathVariable UUID trackId,
            @Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse milestone = milestoneService.createMilestone(trackId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Milestone created successfully", milestone));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getMilestones(
            @PathVariable UUID trackId) {
        List<MilestoneResponse> milestones = milestoneService.getMilestonesByTrack(trackId);
        return ResponseEntity.ok(new ApiResponse<>("Milestones retrieved successfully", milestones));
    }

    @PatchMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(
            @PathVariable UUID trackId,
            @PathVariable UUID milestoneId,
            @Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse milestone = milestoneService.updateMilestone(trackId, milestoneId, request);
        return ResponseEntity.ok(new ApiResponse<>("Milestone updated successfully", milestone));
    }

    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(
            @PathVariable UUID trackId,
            @PathVariable UUID milestoneId) {
        milestoneService.deleteMilestone(trackId, milestoneId);
        return ResponseEntity.ok(new ApiResponse<>("Milestone deleted successfully", null));
    }
}
