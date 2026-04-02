package com.thehalo.halobackend.model.policy;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a monthly insurance policy purchased by an influencer
 * for a specific social media profile on a given product.
 *
 * Business model: Monthly billing only. Policy renews each month
 * upon premium payment. Coverage starts from day of application approval.
 */
@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE policies SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Policy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable reference, e.g. POL-2024-00123 */
    @Column(nullable = false, unique = true)
    private String policyNumber;

    /** The user who purchased this policy */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    /** The specific social media profile being insured */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserPlatform profile;

    /** The product/coverage tier selected */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Combined coverage ceiling = sum of all product sub-limits */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCoverageLimit;

    /** Monthly premium charged, may differ from basePremium due to risk scoring */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.PENDING_PAYMENT;

    /** Next monthly payment due date */
    @Column(nullable = true)
    private LocalDate nextPaymentDueDate;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalPremiumPaid = BigDecimal.ZERO;

    /** Risk score from Underwriter at time of issuance (0-100) */
    private Integer riskScore;

    /** How many times this policy has been renewed (months paid) */
    @Builder.Default
    @Column(nullable = false)
    private Integer renewalCount = 0;

    /** Underwriter who approved/issued the policy */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriter_id")
    private AppUser underwriter;
}
