package org.nashinnov8.multitrack.user.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.ApiResponse;
import org.nashinnov8.multitrack.gamification.dto.response.UserBadgeResponse;
import org.nashinnov8.multitrack.gamification.service.BadgeService;
import org.nashinnov8.multitrack.user.dto.request.UpdateUserRequest;
import org.nashinnov8.multitrack.user.dto.response.UserResponse;
import org.nashinnov8.multitrack.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final BadgeService badgeService;

    public UserController(UserService userService, BadgeService badgeService) {
        this.userService = userService;
        this.badgeService = badgeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>("User profile retrieved successfully", user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(new ApiResponse<>("User profile updated successfully", user));
    }

    @GetMapping("/{id}/badges")
    public ResponseEntity<ApiResponse<List<UserBadgeResponse>>> getUserBadges(@PathVariable UUID id) {
        List<UserBadgeResponse> badges = badgeService.getBadgesForUser(id);
        return ResponseEntity.ok(new ApiResponse<>("User badges retrieved successfully", badges));
    }
}
