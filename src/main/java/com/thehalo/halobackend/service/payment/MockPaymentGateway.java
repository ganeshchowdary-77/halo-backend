package com.thehalo.halobackend.service.payment;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock Payment Gateway for Development
 * Simulates payment processing without external dependencies
 */

@Service
public class MockPaymentGateway {
    
    /**
     * Process a mock payment
     * Always succeeds and generates a mock transaction ID
     */
    public PaymentResult processPayment(BigDecimal amount, String cardLast4) {

        
        // Simulate payment processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate mock transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        

        
        return PaymentResult.success(transactionId, amount);
    }
    
    /**
     * Process a mock refund
     * Always succeeds
     */
    public PaymentResult processRefund(String originalTransactionId, BigDecimal amount) {

        
        String refundId = "RFD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        

        
        return PaymentResult.success(refundId, amount);
    }
    
    /**
     * Payment result wrapper
     */
    public static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final BigDecimal amount;
        private final String message;
        
        private PaymentResult(boolean success, String transactionId, BigDecimal amount, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.amount = amount;
            this.message = message;
        }
        
        public static PaymentResult success(String transactionId, BigDecimal amount) {
            return new PaymentResult(true, transactionId, amount, "Payment processed successfully");
        }
        
        public static PaymentResult failure(String message) {
            return new PaymentResult(false, null, null, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
