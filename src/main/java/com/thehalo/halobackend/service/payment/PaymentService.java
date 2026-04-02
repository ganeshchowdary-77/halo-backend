package com.thehalo.halobackend.service.payment;

import com.thehalo.halobackend.dto.payment.request.AddPaymentMethodRequest;
import com.thehalo.halobackend.dto.payment.request.ProcessPaymentRequest;
import com.thehalo.halobackend.dto.payment.response.PaymentMethodResponse;
import com.thehalo.halobackend.dto.payment.response.PaymentSummaryResponse;
import com.thehalo.halobackend.dto.payment.response.SurrenderValueResponse;
import com.thehalo.halobackend.dto.payment.response.TransactionResponse;

import java.util.List;

public interface PaymentService {

    // Payment Methods
    PaymentMethodResponse addPaymentMethod(AddPaymentMethodRequest request);

    List<PaymentMethodResponse> getMyPaymentMethods();

    void deletePaymentMethod(Long id);

    // Ledger & Billing
    PaymentSummaryResponse getPaymentSummary(Long policyId);

    TransactionResponse processPremiumPayment(Long policyId, ProcessPaymentRequest request);

    List<TransactionResponse> getMyTransactionHistory();

    List<TransactionResponse> getPolicyTransactionHistory(Long policyId);

    // Surrender/Payouts
    SurrenderValueResponse getSurrenderValue(Long policyId);

    TransactionResponse processSurrender(Long policyId);

}
