package org.nashinnov8.multitrack.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.nashinnov8.multitrack.common.domain.BaseEntity;
import org.nashinnov8.multitrack.user.domain.User;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
}
