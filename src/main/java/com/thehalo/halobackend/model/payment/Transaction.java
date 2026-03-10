package com.thehalo.halobackend.model.payment;

import com.thehalo.halobackend.enums.TransactionStatus;
import com.thehalo.halobackend.enums.TransactionType;
import com.thehalo.halobackend.model.base.BaseEntity;
import com.thehalo.halobackend.model.policy.Policy;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Not using soft delete for transactions; financial ledgers must be immutable
// or distinctly reversed
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(unique = true, length = 100)
    private String referenceNumber; // Mock gateway ref (e.g. Stripe PaymentIntent ID)

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime transactionDate = LocalDateTime.now();

}
