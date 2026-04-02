package com.thehalo.halobackend.enums;

public enum TransactionType {
    PREMIUM_PAYMENT,   // Monthly premium — Money In
    LATE_FEE_PAYMENT,  // Late fee charge — Money In
    CLAIM_PAYOUT,      // Approved claim disbursement — Money Out
    SURRENDER_PAYOUT   // Early surrender refund — Money Out
}
