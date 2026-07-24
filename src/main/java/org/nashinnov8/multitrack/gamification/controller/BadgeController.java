package org.nashinnov8.multitrack.gamification.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.nashinnov8.multitrack.common.dto.PaginatedResponse;
import org.nashinnov8.multitrack.gamification.dto.request.BadgeRequest;
import org.nashinnov8.multitrack.gamification.dto.response.BadgeResponse;
import org.nashinnov8.multitrack.gamification.dto.response.UserBadgeResponse;
import org.nashinnov8.multitrack.gamification.service.BadgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(new ApiResponse<>("Badges retrieved successfully", badges));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PaginatedResponse<BadgeResponse>>> getAllBadgesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<BadgeResponse> badges = badgeService.getAllBadges(page, size);
        return ResponseEntity.ok(new ApiResponse<>("Badges retrieved successfully", badges));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BadgeResponse>> createBadge(
            @Valid @RequestBody BadgeRequest request) {
        BadgeResponse badge = badgeService.createBadge(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Badge created successfully", badge));
    }

    @PostMapping("/{badgeId}/award/{userId}")
    public ResponseEntity<ApiResponse<UserBadgeResponse>> awardBadge(
            @PathVariable UUID badgeId,
            @PathVariable UUID userId) {
        UserBadgeResponse userBadge = badgeService.awardBadgeToUser(badgeId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Badge awarded successfully", userBadge));
    }
}
