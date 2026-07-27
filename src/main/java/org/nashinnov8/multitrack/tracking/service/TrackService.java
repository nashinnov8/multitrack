package org.nashinnov8.multitrack.tracking.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.nashinnov8.multitrack.common.dto.PaginatedResponse;
import org.nashinnov8.multitrack.common.exception.ForbiddenException;
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
import org.nashinnov8.multitrack.user.util.LevelCalculator;
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
  private final org.nashinnov8.multitrack.gamification.service.BadgeEvaluatorService badgeEvaluatorService;

  public TrackService(
      TrackRepository trackRepository,
      ActivityLogRepository activityLogRepository,
      UserRepository userRepository,
      ConceptRepository conceptRepository,
      org.nashinnov8.multitrack.gamification.service.BadgeEvaluatorService badgeEvaluatorService) {
    this.trackRepository = trackRepository;
    this.activityLogRepository = activityLogRepository;
    this.userRepository = userRepository;
    this.conceptRepository = conceptRepository;
    this.badgeEvaluatorService = badgeEvaluatorService;
  }

  @Transactional
  public TrackResponse createTrack(TrackCreateRequest request, UUID currentUserId) {
    User existingUser = userRepository
        .findById(currentUserId)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with id: " + currentUserId));

    Track newTrack = Track.builder()
        .name(request.name())
        .description(request.description())
        .user(existingUser)
        .isPublic(request.isPublic())
        .build();

    Track savedTrack = trackRepository.save(newTrack);

    // Auto-award badge for creating first track
    badgeEvaluatorService.evaluateAndAward(existingUser.getId(), "🎯 Khởi đầu Mục tiêu");

    return TrackResponse.from(savedTrack);
  }

  public TrackResponse getTrackById(UUID trackId, UUID currentUserId) {
    Track existingTrack = trackRepository
        .findById(trackId)
        .orElseThrow(() -> new ResourceNotFoundException("Track requested not found"));

    if (!existingTrack.getUser().getId().equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to access this track");
    }

    return TrackResponse.from(existingTrack);
  }

  public List<TrackResponse> getAllTracksForUser(UUID userId, UUID currentUserId) {
    if (!userId.equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to access these tracks");
    }
    List<Track> existingTracks = trackRepository.findByUserId(userId);
    return TrackResponse.fromList(existingTracks);
  }

  public PaginatedResponse<TrackResponse> getAllTracksForUser(UUID userId, int page, int size, UUID currentUserId) {
    if (!userId.equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to access these tracks");
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<TrackResponse> pageResult = trackRepository
        .findByUserId(userId, pageable)
        .map(TrackResponse::from);
    return PaginatedResponse.from(pageResult);
  }

  @Transactional
  public ActivityLogResponse logActivity(UUID trackId, ActivityLogRequest request, UUID currentUserId) {
    Track track = trackRepository
        .findByIdWithUser(trackId) // Use JOIN FETCH to optimize fetch
        .orElseThrow(
            () -> new ResourceNotFoundException("Track not found with id: " + trackId));

    if (!track.getUser().getId().equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to modify this track");
    }

    Concept concept = null;
    if (request.conceptId() != null) {
      concept = conceptRepository
          .findById(request.conceptId())
          .orElseThrow(
              () -> new ResourceNotFoundException(
                  "Concept not found with id: " + request.conceptId()));
    }

    // 1. STREAK & CALENDAR GAME BALANCE
    User user = track.getUser();
    ZoneId userZone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Ho_Chi_Minh");
    LocalDate today = LocalDate.now(userZone);
    LocalDate lastDate = track.getLastActivityAt() != null
            ? track.getLastActivityAt().atZone(userZone).toLocalDate()
            : null;

    boolean isFirstCheckInToday = lastDate == null || !lastDate.equals(today);

    if (isFirstCheckInToday) {
        if (lastDate == null || lastDate.equals(today.minusDays(1))) {
            // Checked in yesterday or brand new ➔ Increment streak +1
            track.setCurrentStreak(track.getCurrentStreak() + 1);
            user.setGlobalStreak(user.getGlobalStreak() + 1);
        } else {
            // Missed > 1 day ➔ Check Streak Freeze Protection!
            if (user.getStreakFreezeCount() > 0) {
                user.setStreakFreezeCount(user.getStreakFreezeCount() - 1);
                // Freeze used, preserve current streak!
            } else {
                track.setCurrentStreak(1);
                user.setGlobalStreak(1);
            }
        }
    }
    // If already checked in today: streak remains unchanged (Max 1 streak day per calendar day)

    track.setLongestStreak(Math.max(track.getLongestStreak(), track.getCurrentStreak()));

    // 2. FEYNMAN QUALITY THRESHOLD CHECK (Min 15 chars)
    String feynmanText = (request.explainSimply() != null ? request.explainSimply() : "")
            + (request.whatLearned() != null ? request.whatLearned() : "")
            + (request.note() != null ? request.note() : "");

    boolean isQualityCheckIn = feynmanText.trim().length() >= 15;
    int expEarned = isQualityCheckIn ? 150 : 20;

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
    user.setLevel(LevelCalculator.calculateLevel(user.getTotalExp()));
    userRepository.save(user);

    // Auto-eval badges (Check-in 1st, Streaks, Level, etc.)
    int totalLogsCount = (int) activityLogRepository.countByTrackUserId(user.getId());
    badgeEvaluatorService.evaluateUserProgress(user, 1, totalLogsCount, 0, 0, 0);

    return ActivityLogResponse.from(savedLog);
  }

  public List<ActivityLogResponse> getGaps(UUID trackId, UUID currentUserId) {
    Track track = trackRepository.findById(trackId)
        .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

    if (!track.getUser().getId().equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to access these gaps");
    }

    return activityLogRepository.findGapsByTrackId(trackId).stream()
        .map(ActivityLogResponse::from)
        .toList();
  }

  public PaginatedResponse<ActivityLogResponse> getGaps(UUID trackId, int page, int size, UUID currentUserId) {
    Track track = trackRepository.findById(trackId)
        .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

    if (!track.getUser().getId().equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to access these gaps");
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

  public PaginatedResponse<ActivityLogResponse> getActivityLogs(UUID trackId, int page, int size, UUID currentUserId) {
    Track track = trackRepository.findById(trackId)
        .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

    if (!track.getUser().getId().equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to access these activity logs");
    }

    Pageable pageable = PageRequest.of(page, size);
    Page<ActivityLogResponse> pageResult = activityLogRepository
        .findByTrackIdOrderByCreatedAtDesc(trackId, pageable)
        .map(ActivityLogResponse::from);
    return PaginatedResponse.from(pageResult);
  }

  @Transactional
  public void deleteTrack(UUID trackId, UUID currentUserId) {
    Track track = trackRepository.findById(trackId)
        .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

    if (!track.getUser().getId().equals(currentUserId)) {
      throw new ForbiddenException("You do not have permission to delete this track");
    }

    trackRepository.delete(track);
  }
}
