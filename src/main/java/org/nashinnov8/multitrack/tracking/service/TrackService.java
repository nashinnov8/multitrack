package org.nashinnov8.multitrack.tracking.service;

import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.ActivityLog;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.repository.ActivityLogRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.nashinnov8.multitrack.tracking.dto.request.ActivityLogRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ActivityLogResponse;
import org.nashinnov8.multitrack.tracking.dto.request.TrackCreateRequest;
import org.nashinnov8.multitrack.tracking.dto.response.TrackResponse;

@Service
public class TrackService {

    private final TrackRepository trackRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final org.nashinnov8.multitrack.tracking.repository.ConceptRepository conceptRepository;

    // Dependency Injection thông qua Constructor
    public TrackService(TrackRepository trackRepository, ActivityLogRepository activityLogRepository,
            UserRepository userRepository, org.nashinnov8.multitrack.tracking.repository.ConceptRepository conceptRepository) {
        this.trackRepository = trackRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.conceptRepository = conceptRepository;
    }

    @Transactional
    public TrackResponse createTrack(TrackCreateRequest request) {
        User existingUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));
                
        Track newTrack = Track.builder()
                .name(request.name())
                .description(request.description())
                .user(existingUser)
                .isPublic(request.isPublic())
                .build();

        Track savedTrack = trackRepository.save(newTrack);
        
        return TrackResponse.from(savedTrack);
    }

    public TrackResponse getTrackById(UUID trackId) {
        // TODO 1: Gọi trackRepository.findById(trackId).
        // TODO 2: Nếu Optional rỗng (không tìm thấy), hãy throw ra ResourceNotFoundException.
        // TODO 3: Map đối tượng Track tìm được sang TrackResponse và trả về.
        return null;
    }

    public List<TrackResponse> getAllTracksForUser(UUID userId) {
        // TODO 1: Tìm tất cả Track của user (ví dụ: trackRepository.findByUserId(userId)).
        // TODO 2: Lặp qua danh sách (hoặc dùng stream().map()) để map Track sang TrackResponse và trả về danh sách.
        return null;
    }

    @Transactional
    public ActivityLogResponse logActivity(UUID trackId, ActivityLogRequest request) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        org.nashinnov8.multitrack.tracking.domain.Concept concept = null;
        if (request.conceptId() != null) {
            concept = conceptRepository.findById(request.conceptId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concept not found with id: " + request.conceptId()));
        }

        ActivityLog newLog = ActivityLog.builder()
                .track(track)
                .concept(concept)
                .note(request.note())
                .whatLearned(request.whatLearned())
                .explainSimply(request.explainSimply())
                .gapsFound(request.gapsFound())
                .expEarned(10)
                .build();

        ActivityLog savedLog = activityLogRepository.save(newLog);

        track.setLastActivityAt(java.time.Instant.now());
        trackRepository.save(track);

        return ActivityLogResponse.from(savedLog);
    }

    public List<ActivityLogResponse> getGaps(UUID trackId) {
        if (!trackRepository.existsById(trackId)) {
            throw new ResourceNotFoundException("Track not found with id: " + trackId);
        }
        return activityLogRepository.findGapsByTrackId(trackId)
                .stream()
                .map(ActivityLogResponse::from)
                .toList();
    }

    public List<TrackResponse> findStaleTracks() {
        // TODO 1: Dùng hàm này cho việc check ngầm mỗi ngày (Scheduler).
        // TODO 2: Lấy ra các Track có trạng thái ACTIVE nhưng lastActivityAt 
        //         đã vượt quá số ngày inactivityThresholdDays.
        // TODO 3: Map danh sách Track tìm được sang List<TrackResponse> và trả về.
        return null;
    }
}
