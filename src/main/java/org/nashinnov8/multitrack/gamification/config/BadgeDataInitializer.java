package org.nashinnov8.multitrack.gamification.config;

import org.nashinnov8.multitrack.gamification.domain.Badge;
import org.nashinnov8.multitrack.gamification.repository.BadgeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BadgeDataInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;
    private final JdbcTemplate jdbcTemplate;

    public BadgeDataInitializer(BadgeRepository badgeRepository, JdbcTemplate jdbcTemplate) {
        this.badgeRepository = badgeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Ensure database schema has streak_freeze_count column
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS streak_freeze_count INT NOT NULL DEFAULT 0;");
        } catch (Exception e) {
            // Ignore if column already exists or DB dialect differs
        }

        List<Badge> defaultBadges = List.of(
            Badge.builder()
                .name("🎯 Khởi đầu Mục tiêu")
                .description("Tạo lộ trình (Track) học tập đầu tiên của bạn")
                .iconUrl("🎯")
                .expReward(100)
                .build(),

            Badge.builder()
                .name("✍️ Lời Giải thích Đầu tiên")
                .description("Hoàn thành lượt điểm danh Kỹ thuật Feynman đầu tiên")
                .iconUrl("✍️")
                .expReward(150)
                .build(),

            Badge.builder()
                .name("🌱 Tân binh Kiên trì")
                .description("Duy trì chuỗi điểm danh Streak 3 ngày liên tiếp")
                .iconUrl("🌱")
                .expReward(100)
                .build(),

            Badge.builder()
                .name("🔥 Ngọn lửa Thói quen")
                .description("Duy trì chuỗi điểm danh Streak 7 ngày liên tiếp")
                .iconUrl("🔥")
                .expReward(300)
                .build(),

            Badge.builder()
                .name("🏆 Chiến binh Thói quen")
                .description("Duy trì chuỗi điểm danh Streak 14 ngày liên tiếp")
                .iconUrl("🏆")
                .expReward(700)
                .build(),

            Badge.builder()
                .name("👑 Huyền thoại Kiên trì")
                .description("Duy trì chuỗi điểm danh Streak 30 ngày liên tiếp")
                .iconUrl("👑")
                .expReward(1500)
                .build(),

            Badge.builder()
                .name("💡 Thầy giáo Nhí")
                .description("Hoàn thành 10 bài check-in giải thích bằng từ ngữ đơn giản")
                .iconUrl("💡")
                .expReward(500)
                .build(),

            Badge.builder()
                .name("🔍 Kẻ Săn Lỗ Hổng")
                .description("Phát hiện & ghi nhận 5 Lỗ hổng kiến thức (Gaps)")
                .iconUrl("🔍")
                .expReward(400)
                .build(),

            Badge.builder()
                .name("💎 Bậc Thầy Khái Niệm")
                .description("Làm chủ (Mastered) 5 Khái niệm bài học")
                .iconUrl("💎")
                .expReward(600)
                .build(),

            Badge.builder()
                .name("🚩 Cột mốc Đầu tiên")
                .description("Hoàn thành Milestone mục tiêu đầu tiên")
                .iconUrl("🚩")
                .expReward(200)
                .build(),

            Badge.builder()
                .name("🎓 Tốt nghiệp Lộ trình")
                .description("Hoàn thành 100% tất cả cột mốc của 1 Lộ trình")
                .iconUrl("🎓")
                .expReward(1000)
                .build(),

            Badge.builder()
                .name("🌟 Vượt Ngưỡng Level 5")
                .description("Tích lũy điểm EXP đạt Cấp độ Level 5")
                .iconUrl("🌟")
                .expReward(500)
                .build()
        );

        for (Badge badge : defaultBadges) {
            if (badgeRepository.findByName(badge.getName()).isEmpty()) {
                badgeRepository.save(badge);
            }
        }
    }
}
