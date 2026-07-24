package org.nashinnov8.multitrack.tracking.service;

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
import org.nashinnov8.multitrack.tracking.domain.Milestone;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.dto.request.MilestoneRequest;
import org.nashinnov8.multitrack.tracking.dto.response.MilestoneResponse;
import org.nashinnov8.multitrack.tracking.repository.MilestoneRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.common.exception.ForbiddenException;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock private MilestoneRepository milestoneRepository;
    @Mock private TrackRepository trackRepository;

    @InjectMocks private MilestoneService milestoneService;

    private User mockUser;
    private Track mockTrack;
    private Milestone mockMilestone;
    private UUID trackId;
    private UUID milestoneId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        trackId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();
        userId = UUID.randomUUID();

        mockUser = User.builder().username("testuser").build();
        mockUser.setId(userId);

        mockTrack = Track.builder().name("Java Mastery").user(mockUser).build();
        mockTrack.setId(trackId);

        mockMilestone = Milestone.builder()
                .track(mockTrack)
                .name("Learn Generics")
                .description("Understand type parameters")
                .isCompleted(false)
                .build();
        mockMilestone.setId(milestoneId);
    }

    // --- createMilestone ---

    @Test
    void createMilestone_Success() {
        // Arrange
        MilestoneRequest request = new MilestoneRequest("Learn Generics", "Understand type parameters", false);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(mockMilestone);

        // Act
        MilestoneResponse response = milestoneService.createMilestone(trackId, request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("Learn Generics", response.name());
        assertFalse(response.isCompleted());
        verify(trackRepository).findById(trackId);
        verify(milestoneRepository).save(any(Milestone.class));
    }

    @Test
    void createMilestone_Failure_TrackNotFound() {
        // Arrange
        MilestoneRequest request = new MilestoneRequest("Learn Generics", null, false);
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.createMilestone(trackId, request, userId));
        verify(milestoneRepository, never()).save(any());
    }

    // --- getMilestonesByTrack ---

    @Test
    void getMilestonesByTrack_Success() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(milestoneRepository.findByTrackId(trackId)).thenReturn(List.of(mockMilestone));

        // Act
        List<MilestoneResponse> responses = milestoneService.getMilestonesByTrack(trackId, userId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Learn Generics", responses.get(0).name());
    }

    @Test
    void getMilestonesByTrack_Failure_TrackNotFound() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.getMilestonesByTrack(trackId, userId));
    }

    // --- updateMilestone ---

    @Test
    void updateMilestone_Success() {
        // Arrange
        MilestoneRequest request = new MilestoneRequest("Learn Generics - Updated", "Deep dive", true);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(mockMilestone));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(mockMilestone);

        // Act
        MilestoneResponse response = milestoneService.updateMilestone(trackId, milestoneId, request, userId);

        // Assert
        assertNotNull(response);
        verify(milestoneRepository).save(mockMilestone);
    }

    @Test
    void updateMilestone_Failure_MilestoneNotFound() {
        // Arrange
        MilestoneRequest request = new MilestoneRequest("Updated", null, true);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.updateMilestone(trackId, milestoneId, request, userId));
    }

    // --- deleteMilestone ---

    @Test
    void deleteMilestone_Success() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(milestoneRepository.existsById(milestoneId)).thenReturn(true);

        // Act
        milestoneService.deleteMilestone(trackId, milestoneId, userId);

        // Assert
        verify(milestoneRepository).deleteById(milestoneId);
    }

    @Test
    void deleteMilestone_Failure_MilestoneNotFound() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(milestoneRepository.existsById(milestoneId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.deleteMilestone(trackId, milestoneId, userId));
        verify(milestoneRepository, never()).deleteById(any());
    }
}
