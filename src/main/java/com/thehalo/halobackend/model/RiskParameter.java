package com.thehalo.halobackend.model;

import com.thehalo.halobackend.enums.Niche;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.profile.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Underwriter-managed risk configuration parameters.
 * Each record defines how a specific risk factor (niche, platform, follower
 * tier)
 * affects the premium multiplier.
 */
@Entity
@Table(name = "risk_parameters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskParameter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Discriminator key, e.g.:
     * NICHE_POLITICS, NICHE_ADULT_CONTENT, FOLLOWER_TIER_1M_PLUS, PLATFORM_TIKTOK
     */
    @Column(nullable = false)
    private String paramKey;

    /** Human-readable label for UI display */
    @Column(nullable = false)
    private String label;

    /**
     * Premium multiplier applied on top of basePremium.
     * 1.0 = no change, 1.5 = 50% surcharge, 0.9 = 10% discount.
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal multiplier;

    /** Optional — restricts this param to a specific niche if set */
    @Enumerated(EnumType.STRING)
    private Niche applicableNiche;

    /** Optional — minimum follower count for this tier to apply */
    private Long minFollowerCount;

    /** Optional — maximum follower count for this tier to apply */
    private Long maxFollowerCount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 500)
    private String description;

    /** Who last updated this parameter */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private AppUser updatedByUser;

    /** Update note explaining why the change was made */
    @Column(length = 1000)
    private String updateNote;

    /** When this parameter came into effect */
    private LocalDate effectiveFrom;
}
