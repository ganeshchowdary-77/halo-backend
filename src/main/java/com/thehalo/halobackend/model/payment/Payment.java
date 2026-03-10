package com.thehalo.halobackend.model.payment;

import com.thehalo.halobackend.enums.PaymentStatus;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.profile.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Premium payment made by an influencer for a Policy.
 * One policy can have multiple monthly payments.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    /** Who made the payment */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser paidBy;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** External payment gateway reference (Stripe ID, etc.) */
    @Column(unique = true)
    private String transactionId;

    /** Payment method: CARD, BANK_TRANSFER, UPI */
    @Column(nullable = false)
    private String paymentMethod;

    /** Which billing cycle this covers, e.g. "2024-03" */
    private String billingPeriod;

    /** Failure reason from payment gateway, if applicable */
    private String failureReason;
}
