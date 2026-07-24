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
import org.nashinnov8.multitrack.tracking.domain.Concept;
import org.nashinnov8.multitrack.tracking.domain.ConceptStatus;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.dto.request.ConceptRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ConceptResponse;
import org.nashinnov8.multitrack.tracking.repository.ConceptRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.common.exception.ForbiddenException;

@ExtendWith(MockitoExtension.class)
class ConceptServiceTest {

    @Mock private ConceptRepository conceptRepository;
    @Mock private TrackRepository trackRepository;

    @InjectMocks private ConceptService conceptService;

    private User mockUser;
    private Track mockTrack;
    private Concept mockConcept;
    private UUID trackId;
    private UUID conceptId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        trackId = UUID.randomUUID();
        conceptId = UUID.randomUUID();
        userId = UUID.randomUUID();

        mockUser = User.builder().username("testuser").build();
        mockUser.setId(userId);

        mockTrack = Track.builder().name("Java Mastery").user(mockUser).build();
        mockTrack.setId(trackId);

        mockConcept = Concept.builder()
                .track(mockTrack)
                .name("Polymorphism")
                .status(ConceptStatus.NOT_UNDERSTOOD)
                .build();
        mockConcept.setId(conceptId);
    }

    // --- createConcept ---

    @Test
    void createConcept_Success_WithDefaultStatus() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism", null);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.save(any(Concept.class))).thenReturn(mockConcept);

        // Act
        ConceptResponse response = conceptService.createConcept(trackId, request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("Polymorphism", response.name());
        assertEquals("NOT_UNDERSTOOD", response.status());
        verify(conceptRepository).save(any(Concept.class));
    }

    @Test
    void createConcept_Success_WithProvidedStatus() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism", ConceptStatus.MASTERED);
        Concept masteredConcept = Concept.builder()
                .track(mockTrack)
                .name("Polymorphism")
                .status(ConceptStatus.MASTERED)
                .build();
        masteredConcept.setId(conceptId);

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.save(any(Concept.class))).thenReturn(masteredConcept);

        // Act
        ConceptResponse response = conceptService.createConcept(trackId, request, userId);

        // Assert
        assertEquals("MASTERED", response.status());
    }

    @Test
    void createConcept_Failure_TrackNotFound() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism", null);
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.createConcept(trackId, request, userId));
        verify(conceptRepository, never()).save(any());
    }

    // --- getConceptsByTrack ---

    @Test
    void getConceptsByTrack_Success() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.findByTrackId(trackId)).thenReturn(List.of(mockConcept));

        // Act
        List<ConceptResponse> responses = conceptService.getConceptsByTrack(trackId, userId);

        // Assert
        assertEquals(1, responses.size());
        assertEquals("Polymorphism", responses.get(0).name());
    }

    @Test
    void getConceptsByTrack_Failure_TrackNotFound() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.getConceptsByTrack(trackId, userId));
    }

    // --- updateConcept ---

    @Test
    void updateConcept_Success() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism Advanced", ConceptStatus.EXPLAINED_WITH_GAPS);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.findById(conceptId)).thenReturn(Optional.of(mockConcept));
        when(conceptRepository.save(any(Concept.class))).thenReturn(mockConcept);

        // Act
        ConceptResponse response = conceptService.updateConcept(trackId, conceptId, request, userId);

        // Assert
        assertNotNull(response);
        verify(conceptRepository).save(mockConcept);
    }

    @Test
    void updateConcept_Failure_ConceptNotFound() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism", null);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.findById(conceptId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.updateConcept(trackId, conceptId, request, userId));
    }

    // --- deleteConcept ---

    @Test
    void deleteConcept_Success() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.existsById(conceptId)).thenReturn(true);

        // Act
        conceptService.deleteConcept(trackId, conceptId, userId);

        // Assert
        verify(conceptRepository).deleteById(conceptId);
    }

    @Test
    void deleteConcept_Failure_ConceptNotFound() {
        // Arrange
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(mockTrack));
        when(conceptRepository.existsById(conceptId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.deleteConcept(trackId, conceptId, userId));
        verify(conceptRepository, never()).deleteById(any());
    }
}
