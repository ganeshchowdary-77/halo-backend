package com.thehalo.halobackend.model.user;

import com.thehalo.halobackend.enums.Niche;
import com.thehalo.halobackend.enums.PlatformVerificationStatus;
import com.thehalo.halobackend.enums.RiskLevel;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.platform.Platform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * A specific social media channel linked to an AppUser for insurance coverage.
 * One user can have multiple platforms across different social media sites.
 */
@Entity
@Table(name = "user_platforms", uniqueConstraints = @UniqueConstraint(columnNames = { "platform_id", "handle" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE user_platforms SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserPlatform extends BaseEntity {

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

    @Column(name = "platform_url")
    private String platformUrl;

    /** The handle of the mock account this was verified against */
    @Column(name = "mock_account_handle")
    private String mockAccountHandle;

    @Column(name = "follower_count", nullable = false)
    private Integer followerCount;

    @Column(name = "engagement_rate", nullable = false)
    private BigDecimal engagementRate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Niche niche;

    /** Custom niche description when niche is OTHER */
    @Column(name = "custom_niche")
    private String customNiche;

    /** Whether The Halo has verified ownership of this platform */
    @Builder.Default
    private Boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlatformVerificationStatus verificationStatus = PlatformVerificationStatus.PENDING;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // ===== GRANULAR VERIFICATION FIELDS =====
    
    /** Niche verification status */
    @Column(name = "niche_verified")
    private Boolean nicheVerified;
    
    /** Niche rejection reason */
    @Column(name = "niche_rejection_reason", length = 500)
    private String nicheRejectionReason;
    
    /** Address proof verification status */
    @Column(name = "address_verified")
    private Boolean addressVerified;
    
    /** Address rejection reason */
    @Column(name = "address_rejection_reason", length = 500)
    private String addressRejectionReason;
    
    /** Income proof verification status */
    @Column(name = "income_verified")
    private Boolean incomeVerified;
    
    /** Income rejection reason */
    @Column(name = "income_rejection_reason", length = 500)
    private String incomeRejectionReason;

    /**
     * AI/Underwriter-computed risk score (0-100).
     * Lower = lower risk → lower premium multiplier.
     * If > threshold (e.g., 70), requires underwriter review
     */
    @Column(name = "risk_score")
    private Integer riskScore;

    // ===== REACH METRICS =====

    /** Calculated reach metrics */
    @Column(name = "estimated_reach")
    private Long estimatedReach;

    @Column(name = "engagement_score", precision = 5, scale = 2)
    private BigDecimal engagementScore;

    // ===== NEW VERIFICATION FIELDS =====

    /** Address proof document relative path */
    @Column(name = "address_proof_path")
    private String addressProofPath;

    /** ID verification document relative path (passport, driver's license, etc.) */
    @Column(name = "id_verification_path")
    private String idVerificationPath;

    /** Income proof document relative path */
    @Column(name = "income_proof_path")
    private String incomeProofPath;

    /** Number of previous claims filed on this platform */
    @Column(name = "previous_claims_count")
    @Builder.Default
    private Integer previousClaimsCount = 0;

    /** Total amount claimed previously on this platform */
    @Column(name = "previous_claims_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal previousClaimsAmount = BigDecimal.ZERO;

    /** Verification notes from IAM Admin */
    @Column(name = "verification_notes", length = 1000)
    private String verificationNotes;

    /** When the platform was verified */
    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    /** IAM Admin who verified this platform */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private AppUser verifiedBy;
}

