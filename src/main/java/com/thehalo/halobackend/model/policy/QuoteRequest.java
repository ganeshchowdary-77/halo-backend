package com.thehalo.halobackend.model.policy;

import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE quote_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class QuoteRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserPlatform profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.PENDING;

    // Custom requests from influencer
    @Column(length = 2000)
    private String notes;

    // ===== NEW QUOTE FLOW FIELDS =====

    /** Calculated premium shown to user */
    @Column(name = "calculated_premium", precision = 19, scale = 2)
    private BigDecimal calculatedPremium;

    /** Final premium after user accepts (may be adjusted by underwriter) */
    @Column(name = "offered_premium", precision = 19, scale = 2)
    private BigDecimal offeredPremium;

    /** Risk score at time of quote (from profile + calculation) */
    @Column(name = "risk_score")
    private Integer riskScore;

    /** Whether this quote requires underwriter review */
    @Column(name = "requires_review")
    @Builder.Default
    private Boolean requiresReview = false;

    /** When user accepted the quote */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(length = 2000)
    private String underwriterNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriter_id")
    private AppUser assignedUnderwriter;

    private LocalDateTime reviewedAt;
}

