package com.thehalo.halobackend.enums;

public enum ClaimStatus {
    /** Influencer has filed the claim — awaiting officer triage */
    SUBMITTED,
    /** Assigned to an officer and currently being investigated */
    UNDER_REVIEW,
    /** Officer needs additional evidence from the influencer */
    PENDING_INFORMATION,
    /** Claim approved; payout will be initiated */
    APPROVED,
    /** Claim denied by officer */
    DENIED,
    /** Payout completed; claim is administratively closed */
    CLOSED;

    /**
     * Check if this status represents a final state (no further changes expected)
     */
    public boolean isFinal() {
        return this == APPROVED || this == DENIED || this == CLOSED;
    }

    /**
     * Check if this status allows modifications
     */
    public boolean isModifiable() {
        return this == SUBMITTED || this == UNDER_REVIEW || this == PENDING_INFORMATION;
    }

    /**
     * Check if this status requires action from claims officer
     */
    public boolean requiresOfficerAction() {
        return this == SUBMITTED || this == UNDER_REVIEW;
    }
}
