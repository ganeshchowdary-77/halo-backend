package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.payment.request.AddPaymentMethodRequest;
import com.thehalo.halobackend.dto.payment.request.ProcessPaymentRequest;
import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.PaymentSummaryResponse;
import com.thehalo.halobackend.dto.payment.response.SurrenderValueResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;
import com.thehalo.halobackend.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // --- Payment Methods ---

    @PostMapping("/methods")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<PaymentMethodResponse>> addPaymentMethod(
            @Valid @RequestBody AddPaymentMethodRequest request) {
        return ResponseFactory.success(paymentService.addPaymentMethod(request), "Payment method saved");
    }

    @GetMapping("/methods")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<List<PaymentMethodResponse>>> getMyPaymentMethods() {
        return ResponseFactory.success(paymentService.getMyPaymentMethods(), "Retrieved payment methods");
    }

    @DeleteMapping("/methods/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<Void>> deletePaymentMethod(@PathVariable Long id) {
        paymentService.deletePaymentMethod(id);
        return ResponseFactory.success(null, "Payment method deleted");
    }

    // --- Ledger & Billing ---

    @GetMapping("/policies/{policyId}/summary")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<PaymentSummaryResponse>> getPaymentSummary(@PathVariable Long policyId) {
        return ResponseFactory.success(paymentService.getPaymentSummary(policyId), "Payment summary retrieved");
    }

    @PostMapping("/policies/{policyId}/charge")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<TransactionResponse>> processPremiumPayment(
            @PathVariable Long policyId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        return ResponseFactory.success(paymentService.processPremiumPayment(policyId, request),
                "Payment processed successfully");
    }

    @GetMapping("/ledger")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<List<TransactionResponse>>> getMyTransactionHistory() {
        return ResponseFactory.success(paymentService.getMyTransactionHistory(), "Ledger history retrieved");
    }

    @GetMapping("/policies/{policyId}/ledger")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<List<TransactionResponse>>> getPolicyTransactionHistory(
            @PathVariable Long policyId) {
        return ResponseFactory.success(paymentService.getPolicyTransactionHistory(policyId), "Policy ledger retrieved");
    }

    // --- Surrender ---

    @GetMapping("/policy/{policyId}/surrender-value")
    @Operation(summary = "Get surrender value", description = "Calculate the potential surrender value for a policy")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> getSurrenderValue(@PathVariable Long policyId) {
        SurrenderValueResponse result = paymentService.getSurrenderValue(policyId);
        return ResponseFactory.success(result, "Surrender value calculated successfully");
    }

    @PostMapping("/policies/{policyId}/surrender")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<TransactionResponse>> processSurrender(@PathVariable Long policyId) {
        return ResponseFactory.success(paymentService.processSurrender(policyId),
                "Policy surrendered and payout issued");
    }

}
