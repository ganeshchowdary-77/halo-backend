package com.thehalo.halobackend.scheduler;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.TransactionStatus;
import com.thehalo.halobackend.enums.TransactionType;
import com.thehalo.halobackend.model.payment.Transaction;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Highly cohesive, standalone job for managing enterprise policy lifecycles.
 * Decoupled from service layer for clean testability according to SOLID
 * principles.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyLifecycleJob {

    private final PolicyRepository policyRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Executes every day at 00:00:00 server time.
     * Evaluates all ACTIVE policies for expiration, overdue payments, and
     * maturities.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void evaluatePolicyLifecycles() {
        log.info("Starting Policy Lifecycle Evaluation Job at {}", LocalDateTime.now());

        LocalDate today = LocalDate.now();
        List<Policy> activePolicies = policyRepository.findByStatus(PolicyStatus.ACTIVE);

        int maturedCount = 0;
        int lapsedCount = 0;

        for (Policy policy : activePolicies) {

            // 1. Check for Maturity
            if (policy.getMaturityDate() != null && !policy.getMaturityDate().isAfter(today)) {
                processMaturity(policy);
                maturedCount++;
                continue;
            }

            // 2. Check for extreme delinquency (Lapse rules - assuming 30 days past due
            // triggers lapse)
            if (policy.getNextPaymentDueDate() != null && policy.getNextPaymentDueDate().plusDays(30).isBefore(today)) {
                processLapse(policy);
                lapsedCount++;
            }
        }

        log.info("Policy Lifecycle Job completed. Matured: {}, Lapsed: {}", maturedCount, lapsedCount);
    }

    private void processMaturity(Policy policy) {
        log.info("Policy {} reached maturity date.", policy.getPolicyNumber());
        policy.setStatus(PolicyStatus.MATURED);
        policy.setEndDate(LocalDate.now());

        Product product = policy.getProduct();
        if (product.getGuaranteedMaturityBenefit() != null
                && product.getGuaranteedMaturityBenefit().compareTo(BigDecimal.ZERO) > 0) {
            // Issue Maturity Payout to Ledger
            Transaction tx = Transaction.builder()
                    .policy(policy)
                    .amount(product.getGuaranteedMaturityBenefit())
                    .transactionType(TransactionType.MATURITY_PAYOUT)
                    .status(TransactionStatus.COMPLETED)
                    .referenceNumber("MATURE-" + UUID.randomUUID().toString().substring(0, 8))
                    .transactionDate(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);
        }

        policyRepository.save(policy);
    }

    private void processLapse(Policy policy) {
        log.info("Policy {} lapsed due to severe non-payment.", policy.getPolicyNumber());
        policy.setStatus(PolicyStatus.LAPSED);
        policy.setEndDate(LocalDate.now());
        policyRepository.save(policy);
    }

}
