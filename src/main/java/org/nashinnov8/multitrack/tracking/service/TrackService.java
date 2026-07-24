package org.nashinnov8.multitrack.tracking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.PaginatedResponse;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.ActivityLog;
import org.nashinnov8.multitrack.tracking.domain.Concept;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.dto.request.ActivityLogRequest;
import org.nashinnov8.multitrack.tracking.dto.request.TrackCreateRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ActivityLogResponse;
import org.nashinnov8.multitrack.tracking.dto.response.TrackResponse;
import org.nashinnov8.multitrack.tracking.repository.ActivityLogRepository;
import org.nashinnov8.multitrack.tracking.repository.ConceptRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackService {

  private final TrackRepository trackRepository;
  private final ActivityLogRepository activityLogRepository;
  private final UserRepository userRepository;
  private final ConceptRepository conceptRepository;

  // Dependency Injection thông qua Constructor
  public TrackService(
      TrackRepository trackRepository,
      ActivityLogRepository activityLogRepository,
      UserRepository userRepository,
      ConceptRepository conceptRepository) {
    this.trackRepository = trackRepository;
    this.activityLogRepository = activityLogRepository;
    this.userRepository = userRepository;
    this.conceptRepository = conceptRepository;
  }

  @Transactional
  public TrackResponse createTrack(TrackCreateRequest request) {
    User existingUser = userRepository
        .findById(request.userId())
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with id: " + request.userId()));

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
    Track existingTrack = trackRepository
        .findById(trackId)
        .orElseThrow(() -> new ResourceNotFoundException("Track requested not found"));

    return TrackResponse.from(existingTrack);
  }

  public List<TrackResponse> getAllTracksForUser(UUID userId) {
    List<Track> existingTracks = trackRepository.findByUserId(userId);
    return TrackResponse.fromList(existingTracks);
  }

  public PaginatedResponse<TrackResponse> getAllTracksForUser(UUID userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<TrackResponse> pageResult = trackRepository
        .findByUserId(userId, pageable)
        .map(TrackResponse::from);
    return PaginatedResponse.from(pageResult);
  }

  @Transactional
  public ActivityLogResponse logActivity(UUID trackId, ActivityLogRequest request) {
    Track track = trackRepository
        .findById(trackId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Track not found with id: " + trackId));

    Concept concept = null;
    if (request.conceptId() != null) {
      concept = conceptRepository
          .findById(request.conceptId())
          .orElseThrow(
              () -> new ResourceNotFoundException(
                  "Concept not found with id: " + request.conceptId()));
    }

    // 1. TÍNH TOÁN STREAK DỰA TRÊN DỮ LIỆU CŨ (TRƯỚC KHI CẬP NHẬT)
    User user = track.getUser();
    ZoneId userZone = ZoneId.of(user.getTimezone()); // Dùng timezone của User
    LocalDate today = LocalDate.now(userZone);
    LocalDate lastDate = track.getLastActivityAt() != null
            ? track.getLastActivityAt().atZone(userZone).toLocalDate()
            : null;

    if (lastDate == null || lastDate.equals(today.minusDays(1))) {
        // Mới chơi, hoặc hôm qua vừa chơi -> Tăng streak
        track.setCurrentStreak(track.getCurrentStreak() + 1);
    } else if (!lastDate.equals(today)) {
        // Bỏ bê quá 1 ngày -> Reset về 1
        track.setCurrentStreak(1);
    }

    // Cập nhật kỷ lục streak dài nhất
    track.setLongestStreak(Math.max(track.getLongestStreak(), track.getCurrentStreak()));

    // 2. TẠO LOG MỚI
    int expEarned = 10;
    ActivityLog newLog = ActivityLog.builder()
            .track(track)
            .concept(concept)
            .note(request.note())
            .whatLearned(request.whatLearned())
            .explainSimply(request.explainSimply())
            .gapsFound(request.gapsFound())
            .expEarned(expEarned)
            .build();

    ActivityLog savedLog = activityLogRepository.save(newLog);

    // 3. CẬP NHẬT DỮ LIỆU LƯU VÀO DB
    track.setLastActivityAt(java.time.Instant.now());
    trackRepository.save(track);

    // Cộng EXP cho user và lưu lại
    user.setTotalExp(user.getTotalExp() + expEarned);
    userRepository.save(user);

    return ActivityLogResponse.from(savedLog);
  }

  public List<ActivityLogResponse> getGaps(UUID trackId) {
    if (!trackRepository.existsById(trackId)) {
      throw new ResourceNotFoundException("Track not found with id: " + trackId);
    }
    return activityLogRepository.findGapsByTrackId(trackId).stream()
        .map(ActivityLogResponse::from)
        .toList();
  }

  public PaginatedResponse<ActivityLogResponse> getGaps(UUID trackId, int page, int size) {
    if (!trackRepository.existsById(trackId)) {
      throw new ResourceNotFoundException("Track not found with id: " + trackId);
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<ActivityLogResponse> pageResult = activityLogRepository
        .findGapsByTrackId(trackId, pageable)
        .map(ActivityLogResponse::from);
    return PaginatedResponse.from(pageResult);
  }

  public List<TrackResponse> findStaleTracks() {
    // TODO 1: Dùng hàm này cho việc check ngầm mỗi ngày (Scheduler).
    // TODO 2: Lấy ra các Track có trạng thái ACTIVE nhưng lastActivityAt
    // đã vượt quá số ngày inactivityThresholdDays.
    // TODO 3: Map danh sách Track tìm được sang List<TrackResponse> và trả về.

    List<Track> staleTracks = trackRepository.findOverdueTracks();
    return TrackResponse.fromList(staleTracks);
  }
}
