package org.nashinnov8.multitrack.tracking.service;

import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.Milestone;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.dto.request.MilestoneRequest;
import org.nashinnov8.multitrack.tracking.dto.response.MilestoneResponse;
import org.nashinnov8.multitrack.tracking.repository.MilestoneRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final TrackRepository trackRepository;

    public MilestoneService(MilestoneRepository milestoneRepository, TrackRepository trackRepository) {
        this.milestoneRepository = milestoneRepository;
        this.trackRepository = trackRepository;
    }

    @Transactional
    public MilestoneResponse createMilestone(UUID trackId, MilestoneRequest request) {
        Track track = trackRepository
                .findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        Milestone milestone = Milestone.builder()
                .track(track)
                .name(request.name())
                .description(request.description())
                .isCompleted(request.isCompleted())
                .build();

        return MilestoneResponse.from(milestoneRepository.save(milestone));
    }

    public List<MilestoneResponse> getMilestonesByTrack(UUID trackId) {
        if (!trackRepository.existsById(trackId)) {
            throw new ResourceNotFoundException("Track not found with id: " + trackId);
        }
        return MilestoneResponse.fromList(milestoneRepository.findByTrackId(trackId));
    }

    @Transactional
    public MilestoneResponse updateMilestone(UUID trackId, UUID milestoneId, MilestoneRequest request) {
        if (!trackRepository.existsById(trackId)) {
            throw new ResourceNotFoundException("Track not found with id: " + trackId);
        }

        Milestone milestone = milestoneRepository
                .findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));

        milestone.setName(request.name());
        milestone.setDescription(request.description());
        milestone.setCompleted(request.isCompleted());

        return MilestoneResponse.from(milestoneRepository.save(milestone));
    }

    @Transactional
    public void deleteMilestone(UUID trackId, UUID milestoneId) {
        if (!trackRepository.existsById(trackId)) {
            throw new ResourceNotFoundException("Track not found with id: " + trackId);
        }

        if (!milestoneRepository.existsById(milestoneId)) {
            throw new ResourceNotFoundException("Milestone not found with id: " + milestoneId);
        }

        milestoneRepository.deleteById(milestoneId);
    }
}
