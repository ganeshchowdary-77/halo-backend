package com.thehalo.halobackend.enums;

public enum PaymentTerm {
    MONTHLY(12, 1.05),
    QUARTERLY(4, 1.02),
    HALF_YEARLY(2, 1.00),
    YEARLY(1, 0.95);

    private final int installments;
    private final double multiplier;

    PaymentTerm(int installments, double multiplier) {
        this.installments = installments;
        this.multiplier = multiplier;
    }

    public int getInstallments() {
        return installments;
    }

    public double getMultiplier() {
        return multiplier;
    }
}