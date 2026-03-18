package com.thehalo.halobackend.model.claim;

import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.enums.ExpenseType;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a defamation loss claim filed against an active policy.
 */
@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE claims SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Claim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable reference, e.g. CLM-2024-00456 */
    @Column(nullable = false, unique = true)
    private String claimNumber;

    /** Policy under which the claim is filed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    /** The specific social media profile that was defamed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserPlatform profile;

    /** Influencer who filed the claim */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filed_by_id", nullable = false)
    private AppUser filedBy;

    /** Date the defamation incident occurred */
    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, length = 3000)
    private String description;

    /** Source URL of the defamatory content */
    private String incidentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseType expenseType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal claimAmount;

    /** Set by Claims Officer on approval */
    @Column(precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    /** Officer's review notes visible to the influencer */
    @Column(length = 2000)
    private String officerComments;

    /** Claims Officer assigned to review this claim */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private AppUser assignedOfficer;

    /** When the officer reviewed (approved/denied) the claim */
    private LocalDateTime reviewedAt;

    /** Supporting documents uploaded with this claim */
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClaimDocument> documents = new ArrayList<>();
}
