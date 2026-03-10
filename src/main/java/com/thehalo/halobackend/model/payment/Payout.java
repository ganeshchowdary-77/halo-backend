package com.thehalo.halobackend.model.payment;

import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.profile.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound payment (settlement) from The Halo to an influencer after a claim is
 * approved.
 */
@Entity
@Table(name = "payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payout extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The approved claim this payout settles */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    /** Who the payout goes to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private AppUser recipient;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** PENDING, PROCESSING, COMPLETED, FAILED */
    @Column(nullable = false)
    private String status;

    /** Payment rail: BANK_TRANSFER, PAYPAL, UPI */
    @Column(nullable = false)
    private String payoutMethod;

    /** Bank/wallet reference from the payout gateway */
    private String payoutReference;

    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;

    /** Failure reason from gateway if status = FAILED */
    private String failureReason;
}
