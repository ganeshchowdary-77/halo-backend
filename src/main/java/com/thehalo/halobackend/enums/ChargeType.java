package com.thehalo.halobackend.enums;

public enum ChargeType {
    LEGAL_CHARGES("Legal defense and representation", "Per incident"),
    PR_CHARGES("Public relations crisis management", "Monthly retainer"),
    MONITOR_CHARGES("24/7 reputation monitoring", "Monthly fee"),
    CRISIS_MANAGEMENT("Emergency response team", "Per activation"),
    REPUTATION_RECOVERY("Reputation rehabilitation services", "Project-based"),
    CONTENT_REMOVAL("Defamatory content removal", "Per request"),
    EXPERT_WITNESS("Expert witness testimony", "Per case");

    private final String description;
    private final String billingType;

    ChargeType(String description, String billingType) {
        this.description = description;
        this.billingType = billingType;
    }

    public String getDescription() {
        return description;
    }

    public String getBillingType() {
        return billingType;
    }
}