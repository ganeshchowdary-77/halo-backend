package com.thehalo.halobackend.model.policy;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PolicyApplication represents an influencer's application for an insurance policy.
 * Risk assessment and premium calculation happen automatically on submission.
 */
@Entity
@Table(name = "quote_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_number", nullable = false, unique = true)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserPlatform profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Security Assessment Fields ──
    @Column(name = "has_two_factor_auth")
    private Boolean hasTwoFactorAuth;

    @Column(name = "password_rotation_frequency")
    private String passwordRotationFrequency;

    @Column(name = "third_party_management")
    private Boolean thirdPartyManagement;

    @Column(name = "sponsored_content_frequency")
    private String sponsoredContentFrequency;

    // ── Auto-Calculated Fields ──
    @Column(name = "calculated_premium", precision = 12, scale = 2)
    private BigDecimal calculatedPremium;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "requires_review")
    private Boolean requiresReview;

    // ── Underwriter Assignment (for high-risk only) ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_underwriter_id")
    private AppUser assignedUnderwriter;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "underwriter_notes", columnDefinition = "TEXT")
    private String underwriterNotes;

    // ── Policy reference (populated when approved) ──
    @Column(name = "policy_id")
    private Long policyId;

    // ── Timestamps ──
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
