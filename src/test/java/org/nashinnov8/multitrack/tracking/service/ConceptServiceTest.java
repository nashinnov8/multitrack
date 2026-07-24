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

@ExtendWith(MockitoExtension.class)
class ConceptServiceTest {

    @Mock private ConceptRepository conceptRepository;
    @Mock private TrackRepository trackRepository;

    @InjectMocks private ConceptService conceptService;

    private Track mockTrack;
    private Concept mockConcept;
    private UUID trackId;
    private UUID conceptId;

    @BeforeEach
    void setUp() {
        trackId = UUID.randomUUID();
        conceptId = UUID.randomUUID();

        mockTrack = Track.builder().name("Java Mastery").build();
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
        ConceptResponse response = conceptService.createConcept(trackId, request);

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
        ConceptResponse response = conceptService.createConcept(trackId, request);

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
                () -> conceptService.createConcept(trackId, request));
        verify(conceptRepository, never()).save(any());
    }

    // --- getConceptsByTrack ---

    @Test
    void getConceptsByTrack_Success() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(conceptRepository.findByTrackId(trackId)).thenReturn(List.of(mockConcept));

        // Act
        List<ConceptResponse> responses = conceptService.getConceptsByTrack(trackId);

        // Assert
        assertEquals(1, responses.size());
        assertEquals("Polymorphism", responses.get(0).name());
    }

    @Test
    void getConceptsByTrack_Failure_TrackNotFound() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.getConceptsByTrack(trackId));
    }

    // --- updateConcept ---

    @Test
    void updateConcept_Success() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism Advanced", ConceptStatus.EXPLAINED_WITH_GAPS);
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(conceptRepository.findById(conceptId)).thenReturn(Optional.of(mockConcept));
        when(conceptRepository.save(any(Concept.class))).thenReturn(mockConcept);

        // Act
        ConceptResponse response = conceptService.updateConcept(trackId, conceptId, request);

        // Assert
        assertNotNull(response);
        verify(conceptRepository).save(mockConcept);
    }

    @Test
    void updateConcept_Failure_ConceptNotFound() {
        // Arrange
        ConceptRequest request = new ConceptRequest("Polymorphism", null);
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(conceptRepository.findById(conceptId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.updateConcept(trackId, conceptId, request));
    }

    // --- deleteConcept ---

    @Test
    void deleteConcept_Success() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(conceptRepository.existsById(conceptId)).thenReturn(true);

        // Act
        conceptService.deleteConcept(trackId, conceptId);

        // Assert
        verify(conceptRepository).deleteById(conceptId);
    }

    @Test
    void deleteConcept_Failure_ConceptNotFound() {
        // Arrange
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(conceptRepository.existsById(conceptId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> conceptService.deleteConcept(trackId, conceptId));
        verify(conceptRepository, never()).deleteById(any());
    }
}
