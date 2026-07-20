package org.nashinnov8.multitrack.tracking.domain;

import org.nashinnov8.multitrack.common.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(columnDefinition = "TEXT")
    private String note;

    private Integer duration;

    @Builder.Default
    @Column(nullable = false)
    private int expEarned = 0;
}
