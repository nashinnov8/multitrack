package org.nashinnov8.multitrack.tracking.domain;

import org.nashinnov8.multitrack.common.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concepts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Concept extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConceptStatus status = ConceptStatus.NOT_UNDERSTOOD;

    @OneToMany(mappedBy = "concept", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ActivityLog> activityLogs = new ArrayList<>();
}
