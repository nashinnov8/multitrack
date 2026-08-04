package org.nashinnov8.multitrack.gamification.service;

import org.nashinnov8.multitrack.gamification.domain.Badge;
import org.nashinnov8.multitrack.gamification.repository.BadgeRepository;
import org.nashinnov8.multitrack.gamification.repository.UserBadgeRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class BadgeEvaluatorService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeService badgeService;
    private final UserRepository userRepository;

    public BadgeEvaluatorService(
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            BadgeService badgeService,
            UserRepository userRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.badgeService = badgeService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void evaluateAndAward(UUID userId, String badgeName) {
        Optional<Badge> badgeOpt = badgeRepository.findByName(badgeName);
        if (badgeOpt.isEmpty()) return;

        Badge badge = badgeOpt.get();
        boolean alreadyEarned = userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId());
        if (!alreadyEarned) {
            badgeService.awardBadgeToUser(badge.getId(), userId);
        }
    }

    @Transactional
    public void evaluateUserProgress(User user, int totalTrackCount, int checkInCount, int gapCount, int masteredConceptCount, int milestoneCompletedCount) {
        UUID userId = user.getId();

        // 1. Check Track Creation
        if (totalTrackCount >= 1) {
            evaluateAndAward(userId, "🎯 Khởi đầu Mục tiêu");
        }

        // 2. Check First Check-in
        if (checkInCount >= 1) {
            evaluateAndAward(userId, "✍️ Lời Giải thích Đầu tiên");
        }

        // 3. Check Streaks
        int streak = user.getGlobalStreak();
        if (streak >= 3) evaluateAndAward(userId, "🌱 Tân binh Kiên trì");
        if (streak >= 7) evaluateAndAward(userId, "🔥 Ngọn lửa Thói quen");
        if (streak >= 14) evaluateAndAward(userId, "🏆 Chiến binh Thói quen");
        if (streak >= 30) evaluateAndAward(userId, "👑 Huyền thoại Kiên trì");

        // 4. Check Check-in Count (Feynman Scholar)
        if (checkInCount >= 10) evaluateAndAward(userId, "💡 Thầy giáo Nhí");

        // 5. Check Gaps Count
        if (gapCount >= 5) evaluateAndAward(userId, "🔍 Kẻ Săn Lỗ Hổng");

        // 6. Check Mastered Concepts Count
        if (masteredConceptCount >= 5) evaluateAndAward(userId, "💎 Bậc Thầy Khái Niệm");

        // 7. Check Milestones Completed Count
        if (milestoneCompletedCount >= 1) evaluateAndAward(userId, "🚩 Cột mốc Đầu tiên");

        // 8. Check User Level
        if (user.getLevel() >= 5) evaluateAndAward(userId, "🌟 Vượt Ngưỡng Level 5");
    }
}
