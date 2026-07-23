package org.nashinnov8.multitrack.gamification.domain;

import org.nashinnov8.multitrack.common.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String iconUrl;

    @Builder.Default
    @Column(nullable = false)
    private int expReward = 0;

    @OneToMany(mappedBy = "badge", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserBadge> userBadges = new ArrayList<>();
}
