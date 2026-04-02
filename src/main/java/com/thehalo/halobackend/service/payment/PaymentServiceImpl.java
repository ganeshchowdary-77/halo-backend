package com.thehalo.halobackend.service.payment;

import com.thehalo.halobackend.dto.payment.request.AddPaymentMethodRequest;
import com.thehalo.halobackend.dto.payment.request.ProcessPaymentRequest;
import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.PaymentSummaryResponse;
import com.thehalo.halobackend.dto.payment.response.SurrenderValueResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.TransactionStatus;
import com.thehalo.halobackend.enums.TransactionType;
import com.thehalo.halobackend.exception.domain.payment.InsufficientPaymentException;
import com.thehalo.halobackend.exception.domain.payment.InvalidPaymentStateException;
import com.thehalo.halobackend.exception.domain.payment.PaymentMethodNotFoundException;
import com.thehalo.halobackend.exception.domain.payment.UnauthorizedPaymentAccessException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.mapper.payment.PaymentMapper;
import com.thehalo.halobackend.model.payment.PaymentMethod;
import com.thehalo.halobackend.model.payment.Transaction;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.PaymentMethodRepository;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.TransactionRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
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
    private final PolicyApplicationRepository applicationRepository;
    private final PaymentMapper paymentMapper;
    private final MockPaymentGateway mockPaymentGateway;

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
        return paymentMapper.toDto(saved);
    }

    @Override
    public List<PaymentMethodResponse> getMyPaymentMethods() {
        return paymentMethodRepository.findByUserId(currentUserId())
                .stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long id) {
        PaymentMethod pm = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new PaymentMethodNotFoundException(id));
        if (!pm.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedPaymentAccessException(id, currentUserId());
        }
        paymentMethodRepository.delete(pm);
    }

    @Override
    public PaymentSummaryResponse getPaymentSummary(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);

        if (policy.getStatus() == PolicyStatus.ACTIVE && policy.getNextPaymentDueDate() != null 
            && !policy.getNextPaymentDueDate().isBefore(LocalDate.now())) {
            return PaymentSummaryResponse.builder()
                    .policyId(policy.getId())
                    .policyNumber(policy.getPolicyNumber())
                    .basePremiumDue(BigDecimal.ZERO)
                    .lateFeesDue(BigDecimal.ZERO)
                    .totalAmountDue(BigDecimal.ZERO)
                    .daysOverdue(0)
                    .nextPaymentDueDate(policy.getNextPaymentDueDate())
                    .build();
        }

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
            throw new InvalidPaymentStateException(
                "Policy " + policy.getPolicyNumber() + " is not in a payable state. Current status: " + policy.getStatus()
            );
        }

        PaymentMethod pm = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new PaymentMethodNotFoundException(request.getPaymentMethodId()));
        verifyOwnership(pm);

        PaymentSummaryResponse summary = getPaymentSummary(policyId);
        if (summary.getTotalAmountDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentStateException("No premium is currently due for this policy.");
        }
        if (request.getAmount().compareTo(summary.getTotalAmountDue()) < 0) {
            throw new InsufficientPaymentException(summary.getTotalAmountDue(), request.getAmount());
        }

        // Process mock payment
        MockPaymentGateway.PaymentResult paymentResult = mockPaymentGateway.processPayment(
            request.getAmount(), 
            pm.getCardLast4()
        );
        
        if (!paymentResult.isSuccess()) {
            throw new InvalidPaymentStateException("Payment processing failed: " + paymentResult.getMessage());
        }

        // Issue transaction with mock payment ID
        Transaction tx = Transaction.builder()
                .policy(policy)
                .amount(request.getAmount())
                .transactionType(TransactionType.PREMIUM_PAYMENT)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(pm)
                .referenceNumber(paymentResult.getTransactionId())
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(tx);

        // Update ledger fields on policy
        policy.setTotalPremiumPaid(policy.getTotalPremiumPaid().add(request.getAmount()));

        boolean wasFirstPayment = false;
        if (policy.getStatus() == PolicyStatus.PENDING_PAYMENT) {
            wasFirstPayment = true;
            policy.setStatus(PolicyStatus.ACTIVE);
            policy.setStartDate(LocalDate.now());
            policy.setEndDate(LocalDate.now().plusMonths(1));
            policy.setNextPaymentDueDate(LocalDate.now().plusMonths(1));
        } else {
            // Monthly renewal — extend by 1 month from current due date
            LocalDate nextDue = policy.getNextPaymentDueDate() != null
                    ? policy.getNextPaymentDueDate().plusMonths(1)
                    : LocalDate.now().plusMonths(1);
            policy.setNextPaymentDueDate(nextDue);
            policy.setEndDate(nextDue);
            policy.setRenewalCount(policy.getRenewalCount() + 1);
        }

        policyRepository.save(policy);

        // Update the corresponding PolicyApplication status to ACTIVE (if this was first payment)
        if (wasFirstPayment) {
            applicationRepository.findByPolicyId(policyId).ifPresent(application -> {
                application.setStatus(PolicyStatus.ACTIVE);
                applicationRepository.save(application);
            });
        }

        return paymentMapper.toDto(savedTx);
    }

    @Override
    public List<TransactionResponse> getMyTransactionHistory() {
        return transactionRepository.findByPolicyUserIdOrderByTransactionDateDesc(currentUserId())
                .stream().map(paymentMapper::toDto).toList();
    }

    @Override
    public List<TransactionResponse> getPolicyTransactionHistory(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);
        return transactionRepository.findByPolicyIdOrderByTransactionDateDesc(policyId)
                .stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SurrenderValueResponse getSurrenderValue(Long policyId) {
        Policy policy = findPolicyOrThrow(policyId);
        verifyOwnership(policy);

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidPaymentStateException("Only active policies can be surrendered");
        }

        BigDecimal totalPaid = policy.getTotalPremiumPaid();
        BigDecimal surrenderValueMultiplier = new BigDecimal("0.50"); // Default 50%
        BigDecimal surrenderValue = totalPaid.multiply(surrenderValueMultiplier).setScale(2,
                RoundingMode.HALF_UP);

        return SurrenderValueResponse.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .totalPremiumPaid(totalPaid)
                .guaranteedMaturityBenefit(BigDecimal.ZERO)
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
            throw new InvalidPaymentStateException("Only active policies can be surrendered");
        }

        SurrenderValueResponse surrenderResponse = getSurrenderValue(policyId);

        Transaction tx = Transaction.builder()
                .policy(policy)
                .amount(surrenderResponse.getEarlySurrenderValue())
                .transactionType(TransactionType.SURRENDER_PAYOUT)
                .status(TransactionStatus.COMPLETED)
                .referenceNumber("PAYOUT-" + UUID.randomUUID().toString().substring(0, 8))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepository.save(tx);

        policy.setStatus(PolicyStatus.SURRENDERED);
        policy.setEndDate(LocalDate.now());
        policyRepository.save(policy);

        return paymentMapper.toDto(savedTx);
    }

    // --- Helpers ---

    private BigDecimal calculateLateFees(Policy policy) {
        if (policy.getNextPaymentDueDate() == null || policy.getNextPaymentDueDate().isAfter(LocalDate.now())
                || policy.getNextPaymentDueDate().isEqual(LocalDate.now())) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(policy.getNextPaymentDueDate(), LocalDate.now());
        BigDecimal dailyRate = new BigDecimal("0.0005"); // Default 0.05%
        BigDecimal penaltyPercentage = dailyRate.multiply(new BigDecimal(daysOverdue));
        return policy.getPremiumAmount().multiply(penaltyPercentage).setScale(2, RoundingMode.HALF_UP);
    }



    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }

    private Policy findPolicyOrThrow(Long id) {
        return policyRepository.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
    }

    private void verifyOwnership(Policy policy) {
        if (!policy.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedPaymentAccessException(policy.getId(), currentUserId());
        }
    }

    private void verifyOwnership(PaymentMethod pm) {
        if (!pm.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedPaymentAccessException(pm.getId(), currentUserId());
        }
    }
}
