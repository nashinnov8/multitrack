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

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock private MilestoneRepository milestoneRepository;
    @Mock private TrackRepository trackRepository;

    @InjectMocks private MilestoneService milestoneService;

    private Track mockTrack;
    private Milestone mockMilestone;
    private UUID trackId;
    private UUID milestoneId;

    @BeforeEach
    void setUp() {
        trackId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();

        mockTrack = Track.builder().name("Java Mastery").build();
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
        MilestoneResponse response = milestoneService.createMilestone(trackId, request);

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
                () -> milestoneService.createMilestone(trackId, request));
        verify(milestoneRepository, never()).save(any());
    }

    // --- getMilestonesByTrack ---

    @Test
    void getMilestonesByTrack_Success() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(milestoneRepository.findByTrackId(trackId)).thenReturn(List.of(mockMilestone));

        // Act
        List<MilestoneResponse> responses = milestoneService.getMilestonesByTrack(trackId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Learn Generics", responses.get(0).name());
    }

    @Test
    void getMilestonesByTrack_Failure_TrackNotFound() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.getMilestonesByTrack(trackId));
    }

    // --- updateMilestone ---

    @Test
    void updateMilestone_Success() {
        // Arrange
        MilestoneRequest request = new MilestoneRequest("Learn Generics - Updated", "Deep dive", true);
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(mockMilestone));
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(mockMilestone);

        // Act
        MilestoneResponse response = milestoneService.updateMilestone(trackId, milestoneId, request);

        // Assert
        assertNotNull(response);
        verify(milestoneRepository).save(mockMilestone);
    }

    @Test
    void updateMilestone_Failure_MilestoneNotFound() {
        // Arrange
        MilestoneRequest request = new MilestoneRequest("Updated", null, true);
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.updateMilestone(trackId, milestoneId, request));
    }

    // --- deleteMilestone ---

    @Test
    void deleteMilestone_Success() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(milestoneRepository.existsById(milestoneId)).thenReturn(true);

        // Act
        milestoneService.deleteMilestone(trackId, milestoneId);

        // Assert
        verify(milestoneRepository).deleteById(milestoneId);
    }

    @Test
    void deleteMilestone_Failure_MilestoneNotFound() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(milestoneRepository.existsById(milestoneId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.deleteMilestone(trackId, milestoneId));
        verify(milestoneRepository, never()).deleteById(any());
    }
}
