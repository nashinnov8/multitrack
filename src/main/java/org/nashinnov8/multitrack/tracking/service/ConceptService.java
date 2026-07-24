package org.nashinnov8.multitrack.tracking.service;

import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.exception.ForbiddenException;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.Concept;
import org.nashinnov8.multitrack.tracking.domain.ConceptStatus;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.dto.request.ConceptRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ConceptResponse;
import org.nashinnov8.multitrack.tracking.repository.ConceptRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConceptService {

    private final ConceptRepository conceptRepository;
    private final TrackRepository trackRepository;

    public ConceptService(ConceptRepository conceptRepository, TrackRepository trackRepository) {
        this.conceptRepository = conceptRepository;
        this.trackRepository = trackRepository;
    }

    @Transactional
    public ConceptResponse createConcept(UUID trackId, ConceptRequest request, UUID currentUserId) {
        Track track = trackRepository
                .findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        if (!track.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to modify this track");
        }

        ConceptStatus status = request.status() != null ? request.status() : ConceptStatus.NOT_UNDERSTOOD;

        Concept concept = Concept.builder()
                .track(track)
                .name(request.name())
                .status(status)
                .build();

        return ConceptResponse.from(conceptRepository.save(concept));
    }

    public List<ConceptResponse> getConceptsByTrack(UUID trackId, UUID currentUserId) {
        Track track = trackRepository
                .findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        if (!track.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to view these concepts");
        }
        return ConceptResponse.fromList(conceptRepository.findByTrackId(trackId));
    }

    @Transactional
    public ConceptResponse updateConcept(UUID trackId, UUID conceptId, ConceptRequest request, UUID currentUserId) {
        Track track = trackRepository
                .findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        if (!track.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to modify this track");
        }

        Concept concept = conceptRepository
                .findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept not found with id: " + conceptId));

        concept.setName(request.name());
        if (request.status() != null) {
            concept.setStatus(request.status());
        }

        return ConceptResponse.from(conceptRepository.save(concept));
    }

    @Transactional
    public void deleteConcept(UUID trackId, UUID conceptId, UUID currentUserId) {
        Track track = trackRepository
                .findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        if (!track.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to modify this track");
        }

        if (!conceptRepository.existsById(conceptId)) {
            throw new ResourceNotFoundException("Concept not found with id: " + conceptId);
        }

        conceptRepository.deleteById(conceptId);
    }
}
