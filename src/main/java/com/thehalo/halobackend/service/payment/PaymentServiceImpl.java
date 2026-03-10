package com.thehalo.halobackend.service.payment;

import com.thehalo.halobackend.dto.payment.request.AddPaymentMethodRequest;
import com.thehalo.halobackend.dto.payment.request.ProcessPaymentRequest;
import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.PaymentSummaryResponse;
import com.thehalo.halobackend.dto.payment.response.SurrenderQuoteResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;
import com.thehalo.halobackend.enums.BillingCycle;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.TransactionStatus;
import com.thehalo.halobackend.enums.TransactionType;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.mapper.payment.PaymentMapper;
import com.thehalo.halobackend.model.payment.PaymentMethod;
import com.thehalo.halobackend.model.payment.Transaction;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.repository.PaymentMethodRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.TransactionRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.system.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final TransactionRepository transactionRepository;
    private final PolicyRepository policyRepository;
    private final AuditLogService auditLogService;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentMethodResponse addPaymentMethod(AddPaymentMethodRequest request) {
        AppUser user = new AppUser();
        user.setId(currentUserId());

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // Unset other defaults
            List<PaymentMethod> existing = paymentMethodRepository.findByUserId(user.getId());
            for (PaymentMethod pm : existing) {
                pm.setIsDefault(false);
                paymentMethodRepository.save(pm);
            }
        }

        PaymentMethod pm = PaymentMethod.builder()
                .user(user)
                .cardBrand(request.getCardBrand())
                .cardLast4(request.getCardLast4())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .isDefault(request.getIsDefault())
                .build();

        PaymentMethod saved = paymentMethodRepository.save(pm);
        auditLogService.logAction("PAYMENT_METHOD", saved.getId().toString(), "CREATE",
                "Added new card ending in " + saved.getCardLast4());
        return paymentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getMyPaymentMethods() {
        return paymentMethodRepository.findByUserId(currentUserId())
                .stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long id) {
        PaymentMethod pm = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Method not found"));
        if (!pm.getUser().getId().equals(currentUserId())) {
            throw new RuntimeException("Unauthorized");
        }
        paymentMethodRepository.delete(pm);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummary(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);

        BigDecimal basePremium = policy.getPremiumAmount();
        BigDecimal lateFees = calculateLateFees(policy);

        long daysOverdue = 0;
        if (policy.getNextPaymentDueDate() != null && policy.getNextPaymentDueDate().isBefore(LocalDate.now())) {
            daysOverdue = ChronoUnit.DAYS.between(policy.getNextPaymentDueDate(), LocalDate.now());
        }

        return PaymentSummaryResponse.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .basePremiumDue(basePremium)
                .lateFeesDue(lateFees)
                .totalAmountDue(basePremium.add(lateFees))
                .daysOverdue((int) daysOverdue)
                .nextPaymentDueDate(policy.getNextPaymentDueDate())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse processPremiumPayment(Long policyId, ProcessPaymentRequest request) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);

        if (policy.getStatus() != PolicyStatus.PENDING_PAYMENT && policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new RuntimeException("Policy is not in a payable state");
        }

        PaymentMethod pm = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Method not found"));
        verifyOwnership(pm);

        PaymentSummaryResponse summary = getPaymentSummary(policyId);
        if (request.getAmount().compareTo(summary.getTotalAmountDue()) < 0) {
            throw new RuntimeException("Payment amount must cover base premium plus late fees");
        }

        // Issue mock transaction
        Transaction tx = Transaction.builder()
                .policy(policy)
                .amount(request.getAmount())
                .transactionType(TransactionType.PREMIUM_PAYMENT)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(pm)
                .referenceNumber("CHRG-" + UUID.randomUUID().toString().substring(0, 8))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(tx);

        // Update ledger fields on policy
        policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(request.getAmount()));

        if (policy.getStatus() == PolicyStatus.PENDING_PAYMENT) {
            policy.setStatus(PolicyStatus.ACTIVE);
            policy.setStartDate(LocalDate.now());
            // Compute maturity based on product term
            policy.setMaturityDate(LocalDate.now().plusMonths(policy.getProduct().getMaturityTermMonths()));
            policy.setNextPaymentDueDate(computeNextDueDate(LocalDate.now(), policy.getBillingCycle()));
        } else {
            policy.setNextPaymentDueDate(computeNextDueDate(policy.getNextPaymentDueDate(), policy.getBillingCycle()));
        }

        policyRepository.save(policy);
        auditLogService.logAction("TRANSACTION", savedTx.getId().toString(), "PAYMENT",
                "Processed premium payment for policy " + policy.getPolicyNumber());

        return paymentMapper.toDto(savedTx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactionHistory() {
        return transactionRepository.findByPolicyUserIdOrderByTransactionDateDesc(currentUserId())
                .stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getPolicyTransactionHistory(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);
        return transactionRepository.findByPolicyIdOrderByTransactionDateDesc(policyId)
                .stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SurrenderQuoteResponse getSurrenderQuote(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new RuntimeException("Only active policies can be surrendered");
        }

        Product product = policy.getProduct();
        BigDecimal totalPaid = policy.getTotalPremiumPaid();
        BigDecimal surrenderValue = totalPaid.multiply(product.getSurrenderValueMultiplier()).setScale(2,
                RoundingMode.HALF_UP);

        return SurrenderQuoteResponse.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .totalPremiumPaid(totalPaid)
                .guaranteedMaturityBenefit(
                        product.getGuaranteedMaturityBenefit() != null ? product.getGuaranteedMaturityBenefit()
                                : BigDecimal.ZERO)
                .earlySurrenderValue(surrenderValue)
                .warningMessage("Warning: Surrendering early will result in a loss of "
                        + totalPaid.subtract(surrenderValue) + " compared to premiums paid.")
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse processSurrender(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new RuntimeException("Only active policies can be surrendered");
        }

        SurrenderQuoteResponse quote = getSurrenderQuote(policyId);

        Transaction tx = Transaction.builder()
                .policy(policy)
                .amount(quote.getEarlySurrenderValue())
                .transactionType(TransactionType.SURRENDER_PAYOUT)
                .status(TransactionStatus.COMPLETED)
                .referenceNumber("PAYOUT-" + UUID.randomUUID().toString().substring(0, 8))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(tx);

        policy.setStatus(PolicyStatus.SURRENDERED);
        policy.setEndDate(LocalDate.now());
        policyRepository.save(policy);

        auditLogService.logAction("POLICY", policy.getId().toString(), "SURRENDER",
                "Policy surrendered for value " + quote.getEarlySurrenderValue());

        return paymentMapper.toDto(savedTx);
    }

    // --- Helpers ---

    private BigDecimal calculateLateFees(Policy policy) {
        if (policy.getNextPaymentDueDate() == null || policy.getNextPaymentDueDate().isAfter(LocalDate.now())
                || policy.getNextPaymentDueDate().isEqual(LocalDate.now())) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(policy.getNextPaymentDueDate(), LocalDate.now());
        BigDecimal dailyRate = policy.getProduct().getLatePaymentDailyInterestRate();
        BigDecimal penaltyPercentage = dailyRate.multiply(new BigDecimal(daysOverdue));
        return policy.getPremiumAmount().multiply(penaltyPercentage).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate computeNextDueDate(LocalDate fromDate, BillingCycle cycle) {
        if (cycle == BillingCycle.MONTHLY) {
            return fromDate.plusMonths(1);
        } else {
            return fromDate.plusYears(1);
        }
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }

    private Policy findPolicyOrThrow(Long id) {
        return policyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
    }

    private void verifyOwnership(Policy policy) {
        if (!policy.getUser().getId().equals(currentUserId())) {
            throw new RuntimeException("Unauthorized API access to this policy.");
        }
    }

    private void verifyOwnership(PaymentMethod pm) {
        if (!pm.getUser().getId().equals(currentUserId())) {
            throw new RuntimeException("Unauthorized API access to this payment method.");
        }
    }
}
