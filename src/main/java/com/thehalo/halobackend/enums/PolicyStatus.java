package com.thehalo.halobackend.enums;

public enum PolicyStatus {
    // Application phase
    UNDER_REVIEW,          // High-risk application, auto-assigned to underwriter
    APPLICATION_REJECTED,  // Underwriter rejected the application

    // Policy lifecycle (monthly model)
    PENDING_PAYMENT,       // Approved (auto or by underwriter), awaiting first premium
    ACTIVE,                // Premium paid, policy is active for the current month
    EXPIRED,               // Monthly term ended without renewal
    CANCELLED,             // Cancelled by influencer or admin
    SURRENDERED            // Early surrender with partial refund
}
