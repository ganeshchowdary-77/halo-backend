package com.thehalo.halobackend.model.profile;

import com.thehalo.halobackend.enums.Niche;
import com.thehalo.halobackend.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * A specific social media channel linked to an AppUser for insurance coverage.
 * One user can have multiple profiles across different platforms.
 */
@Entity
@Table(name = "user_profiles", uniqueConstraints = @UniqueConstraint(columnNames = { "platform_id", "handle" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE user_profiles SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    /** e.g. @jane_creates */
    @Column(nullable = false)
    private String handle;

    /** Public URL of the profile, e.g. https://instagram.com/jane_creates */
    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "follower_count", nullable = false)
    private Integer followerCount;

    @Column(name = "engagement_rate", nullable = false)
    private BigDecimal engagementRate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Niche niche;

    /** Whether The Halo has verified ownership of this profile */
    @Builder.Default
    private Boolean verified = false;

    /**
     * AI/Underwriter-computed risk score (0-100).
     * Lower = lower risk → lower premium multiplier.
     */
    @Column(name = "risk_score")
    private Integer riskScore;
}
