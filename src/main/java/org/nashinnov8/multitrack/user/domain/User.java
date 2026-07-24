package org.nashinnov8.multitrack.user.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.nashinnov8.multitrack.common.domain.BaseEntity;
import org.nashinnov8.multitrack.gamification.domain.UserBadge;
import org.nashinnov8.multitrack.tracking.domain.Track;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String displayName;

  @Builder.Default
  @Column(nullable = false)
  private int totalExp = 0;

  @Builder.Default
  @Column(nullable = false)
  private int level = 1;

  @Builder.Default
  @Column(nullable = false)
  private int globalStreak = 0;

  @Builder.Default
  @Column(nullable = false)
  private String timezone = "Asia/Ho_Chi_Minh";

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Track> tracks = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<UserBadge> userBadges = new ArrayList<>();
}
