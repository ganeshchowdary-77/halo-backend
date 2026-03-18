package com.thehalo.halobackend.enums;

public enum RiskLevel {
    LOW(0.8),
    MEDIUM(1.0),
    HIGH(1.3),
    VERY_HIGH(1.6);

    private final double multiplier;

    RiskLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}