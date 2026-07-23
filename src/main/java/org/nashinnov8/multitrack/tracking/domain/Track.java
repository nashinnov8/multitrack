package org.nashinnov8.multitrack.tracking.domain;

import org.nashinnov8.multitrack.common.domain.BaseEntity;
import org.nashinnov8.multitrack.user.domain.User;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(unique = true)
    private String slug;

    @Builder.Default
    @Column(nullable = false)
    private boolean isPublic = false;

    private Instant lastActivityAt;

    @Builder.Default
    @Column(nullable = false)
    private int currentStreak = 0;

    @Builder.Default
    @Column(nullable = false)
    private int longestStreak = 0;

    @Builder.Default
    @Column(nullable = false)
    private int inactivityThresholdDays = 7;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TrackStatus status = TrackStatus.ACTIVE;

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityLog> activityLogs = new ArrayList<>();
}