package org.nashinnov8.multitrack.tracking.domain;

import org.nashinnov8.multitrack.common.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "milestones")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Milestone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean isCompleted = false;
}
