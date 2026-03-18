package com.thehalo.halobackend.service.payment;

import com.thehalo.halobackend.dto.payment.request.AddPaymentMethodRequest;
import com.thehalo.halobackend.dto.payment.request.ProcessPaymentRequest;
import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.PaymentSummaryResponse;
import com.thehalo.halobackend.dto.payment.response.SurrenderQuoteResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.TransactionStatus;
import com.thehalo.halobackend.enums.TransactionType;
import com.thehalo.halobackend.exception.domain.payment.PaymentMethodNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.mapper.payment.PaymentMapper;
import com.thehalo.halobackend.model.payment.PaymentMethod;
import com.thehalo.halobackend.model.payment.Transaction;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.PaymentMethodRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.TransactionRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.payment.MockPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private MockPaymentGateway mockPaymentGateway;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private AppUser testUser;
    private Policy testPolicy;
    private PaymentMethod testPaymentMethod;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUserId()).thenReturn(1L);
        SecurityContextHolder.setContext(securityContext);

        testUser = AppUser.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("John Doe")
                .build();

        testPolicy = Policy.builder()
                .id(1L)
                .user(testUser)
                .status(PolicyStatus.ACTIVE)
                .premiumAmount(BigDecimal.valueOf(100))
                .totalPremiumPaid(BigDecimal.ZERO)
                .build();

        testPaymentMethod = PaymentMethod.builder()
                .id(1L)
                .user(testUser)
                .cardLast4("1234")
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .policy(testPolicy)
                .amount(BigDecimal.valueOf(100))
                .transactionType(TransactionType.PREMIUM_PAYMENT)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    void addPaymentMethod_ShouldCreatePaymentMethod() {
        AddPaymentMethodRequest request = AddPaymentMethodRequest.builder()
                .cardBrand("Visa")
                .cardLast4("4242")
                .expiryMonth(12)
                .expiryYear(2025)
                .isDefault(true)
                .build();
        
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(testPaymentMethod);
        when(paymentMapper.toDto(any(PaymentMethod.class))).thenReturn(new PaymentMethodResponse());

        PaymentMethodResponse result = paymentService.addPaymentMethod(request);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository).save(any(PaymentMethod.class));
    }

    @Test
    void getMyPaymentMethods_ShouldReturnList() {
        when(paymentMethodRepository.findByUserId(1L)).thenReturn(List.of(testPaymentMethod));
        when(paymentMapper.toDto(any(PaymentMethod.class))).thenReturn(new PaymentMethodResponse());

        List<PaymentMethodResponse> result = paymentService.getMyPaymentMethods();

        assertThat(result).hasSize(1);
        verify(paymentMethodRepository).findByUserId(1L);
    }

    @Test
    void deletePaymentMethod_ShouldDeleteMethod() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(testPaymentMethod));

        paymentService.deletePaymentMethod(1L);

        verify(paymentMethodRepository).delete(testPaymentMethod);
    }

    @Test
    void deletePaymentMethod_ShouldThrowException_WhenNotFound() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.deletePaymentMethod(1L))
                .isInstanceOf(PaymentMethodNotFoundException.class);
    }

    @Test
    void getPaymentSummary_ShouldReturnSummary() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        PaymentSummaryResponse result = paymentService.getPaymentSummary(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPolicyId()).isEqualTo(1L);
    }

    @Test
    void processPremiumPayment_ShouldCreateTransaction() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .paymentMethodId(1L)
                .amount(BigDecimal.valueOf(100))
                .build();
                
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(testPaymentMethod));
        
        MockPaymentGateway.PaymentResult paymentResult = MockPaymentGateway.PaymentResult.success("REF-123", BigDecimal.valueOf(100));
        when(mockPaymentGateway.processPayment(any(), any())).thenReturn(paymentResult);
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(paymentMapper.toDto(any(Transaction.class))).thenReturn(new TransactionResponse());

        TransactionResponse result = paymentService.processPremiumPayment(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(any(Transaction.class));
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    void getMyTransactionHistory_ShouldReturnList() {
        when(transactionRepository.findByPolicyUserIdOrderByTransactionDateDesc(1L)).thenReturn(List.of(testTransaction));
        when(paymentMapper.toDto(any(Transaction.class))).thenReturn(new TransactionResponse());

        List<TransactionResponse> result = paymentService.getMyTransactionHistory();

        assertThat(result).hasSize(1);
        verify(transactionRepository).findByPolicyUserIdOrderByTransactionDateDesc(1L);
    }

    @Test
    void getSurrenderQuote_ShouldReturnQuote() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        SurrenderQuoteResponse result = paymentService.getSurrenderQuote(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPolicyId()).isEqualTo(1L);
    }

    @Test
    void processSurrender_ShouldCreateRefundTransaction() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);
        when(paymentMapper.toDto(any(Transaction.class))).thenReturn(new TransactionResponse());

        TransactionResponse result = paymentService.processSurrender(1L);

        assertThat(result).isNotNull();
        verify(policyRepository).save(argThat(policy -> 
            policy.getStatus() == PolicyStatus.SURRENDERED
        ));
    }
}
