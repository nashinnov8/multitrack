package org.nashinnov8.multitrack.gamification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.gamification.domain.Badge;
import org.nashinnov8.multitrack.gamification.domain.UserBadge;
import org.nashinnov8.multitrack.gamification.dto.request.BadgeRequest;
import org.nashinnov8.multitrack.gamification.dto.response.BadgeResponse;
import org.nashinnov8.multitrack.gamification.dto.response.UserBadgeResponse;
import org.nashinnov8.multitrack.gamification.repository.BadgeRepository;
import org.nashinnov8.multitrack.gamification.repository.UserBadgeRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BadgeService badgeService;

    private Badge mockBadge;
    private User mockUser;
    private UserBadge mockUserBadge;
    private UUID badgeId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        badgeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        mockBadge = Badge.builder()
                .name("First Steps")
                .description("Complete your first check-in")
                .iconUrl("https://cdn.example.com/badge.png")
                .expReward(100)
                .build();
        mockBadge.setId(badgeId);

        mockUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .displayName("Test User")
                .totalExp(0)
                .build();
        mockUser.setId(userId);

        mockUserBadge = UserBadge.builder()
                .badge(mockBadge)
                .user(mockUser)
                .earnedAt(java.time.Instant.now())
                .build();
        mockUserBadge.setId(UUID.randomUUID());
    }

    // --- getAllBadges ---

    @Test
    void getAllBadges_Success() {
        // Arrange
        when(badgeRepository.findAll()).thenReturn(List.of(mockBadge));

        // Act
        List<BadgeResponse> responses = badgeService.getAllBadges();

        // Assert
        assertEquals(1, responses.size());
        assertEquals("First Steps", responses.get(0).name());
        assertEquals(100, responses.get(0).expReward());
    }

    // --- createBadge ---

    @Test
    void createBadge_Success() {
        // Arrange
        BadgeRequest request = new BadgeRequest("First Steps", "Complete your first check-in", "https://cdn.example.com/badge.png", 100);
        when(badgeRepository.save(any(Badge.class))).thenReturn(mockBadge);

        // Act
        BadgeResponse response = badgeService.createBadge(request);

        // Assert
        assertNotNull(response);
        assertEquals("First Steps", response.name());
        assertEquals(100, response.expReward());
        verify(badgeRepository).save(any(Badge.class));
    }

    // --- awardBadgeToUser ---

    @Test
    void awardBadgeToUser_Success() {
        // Arrange
        when(badgeRepository.findById(badgeId)).thenReturn(Optional.of(mockBadge));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userBadgeRepository.save(any(UserBadge.class))).thenReturn(mockUserBadge);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        UserBadgeResponse response = badgeService.awardBadgeToUser(badgeId, userId);

        // Assert
        assertNotNull(response);
        assertEquals("First Steps", response.badgeName());
        assertEquals(100, response.expReward());
        // Verify user EXP was updated
        assertEquals(100, mockUser.getTotalExp());
        verify(userRepository).save(mockUser);
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void awardBadgeToUser_Failure_BadgeNotFound() {
        // Arrange
        when(badgeRepository.findById(badgeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> badgeService.awardBadgeToUser(badgeId, userId));
        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void awardBadgeToUser_Failure_UserNotFound() {
        // Arrange
        when(badgeRepository.findById(badgeId)).thenReturn(Optional.of(mockBadge));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> badgeService.awardBadgeToUser(badgeId, userId));
        verify(userBadgeRepository, never()).save(any());
    }

    // --- getBadgesForUser ---

    @Test
    void getBadgesForUser_Success() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userBadgeRepository.findByUserId(userId)).thenReturn(List.of(mockUserBadge));

        // Act
        List<UserBadgeResponse> responses = badgeService.getBadgesForUser(userId);

        // Assert
        assertEquals(1, responses.size());
        assertEquals("First Steps", responses.get(0).badgeName());
    }

    @Test
    void getBadgesForUser_Failure_UserNotFound() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> badgeService.getBadgesForUser(userId));
    }
}
