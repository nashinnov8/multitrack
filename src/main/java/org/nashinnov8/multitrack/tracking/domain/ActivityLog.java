package org.nashinnov8.multitrack.tracking.domain;

import jakarta.persistence.*;
import lombok.*;
import org.nashinnov8.multitrack.common.domain.BaseEntity;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "concept_id")
  private Concept concept;

  @Column(columnDefinition = "TEXT")
  private String note;

  @Column(columnDefinition = "TEXT")
  private String whatLearned;

  @Column(columnDefinition = "TEXT")
  private String explainSimply;

  @Column(columnDefinition = "TEXT")
  private String gapsFound;

  private Integer duration;

  @Builder.Default
  @Column(nullable = false)
  private int expEarned = 0;
}
