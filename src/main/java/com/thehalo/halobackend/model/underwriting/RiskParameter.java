package com.thehalo.halobackend.model.underwriting;

import com.thehalo.halobackend.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Underwriter-managed risk configuration parameters.
 * Each record defines how a specific risk factor (platform, niche, etc.)
 * affects the premium multiplier.
 * 
 * Examples:
 * - PLATFORM_INSTAGRAM: 0.80 (Instagram has lower risk)
 * - NICHE_CRYPTO: 1.40 (Crypto niche has higher risk)
 * - PLATFORM_TWITTER: 1.10 (Twitter has higher risk)
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
     * Unique key identifying the parameter
     * Examples: PLATFORM_INSTAGRAM, NICHE_CRYPTO, PLATFORM_TWITTER
     */
    @Column(nullable = false, unique = true, length = 100)
    private String paramKey;

    /**
     * Premium multiplier applied to base premium
     * 1.0 = no change, 1.5 = 50% increase, 0.8 = 20% discount
     */
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal multiplier;

    /**
     * Human-readable description of what this parameter controls
     */
    @Column(length = 500)
    private String description;

    /**
     * Whether this parameter is currently active
     * Inactive parameters are ignored in calculations
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Email/username of who last modified this parameter
     */
    @Column(length = 100)
    private String lastModifiedBy;

    /**
     * When this parameter was last modified
     */
    private LocalDateTime lastModifiedDate;

    /**
     * Optional note explaining why the change was made
     */
    @Column(length = 1000)
    private String updateNote;
}
