package org.nashinnov8.multitrack.common.scheduler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nashinnov8.multitrack.common.service.EmailService;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

  private final TrackRepository trackRepository;
  private final EmailService emailService;

  // Run every day at 20:00 (8 PM)
  @Scheduled(cron = "0 0 20 * * *")
  @Transactional(readOnly = true)
  public void sendDailyStaleTrackReminders() {
    log.info("Starting daily stale track email notification job...");

    List<Track> overdueTracks = trackRepository.findOverdueTracks();
    if (overdueTracks.isEmpty()) {
      log.info("No overdue tracks found. Notification job completed.");
      return;
    }

    // Group overdue tracks by User
    Map<User, List<Track>> tracksByUser = overdueTracks.stream()
        .collect(Collectors.groupingBy(Track::getUser));

    int sentCount = 0;
    for (Map.Entry<User, List<Track>> entry : tracksByUser.entrySet()) {
      User user = entry.getKey();
      List<Track> staleTracks = entry.getValue();

      // Only send if user has verified email & has email address
      if (user.isEnabled() && user.getEmail() != null && !user.getEmail().isBlank()) {
        emailService.sendStaleReminderEmail(
            user.getEmail(),
            user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
            staleTracks
        );
        sentCount++;
      }
    }

    log.info("Daily stale track email notification job finished. Sent emails to {} users.", sentCount);
  }
}
