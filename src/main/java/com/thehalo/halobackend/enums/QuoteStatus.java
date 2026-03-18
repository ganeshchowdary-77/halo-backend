package com.thehalo.halobackend.enums;

public enum QuoteStatus {
    // New flow statuses
    CALCULATED,         // Price calculated, shown to user
    PENDING,           // User accepted, waiting for underwriter review (replaces both PENDING and UNDER_REVIEW)
    APPROVED,          // Auto-approved or underwriter approved
    REJECTED,          // Underwriter rejected
    ACCEPTED,          // User accepted final quote (ready for purchase)
    CONVERTED_TO_POLICY, // Quote successfully converted to policy
    EXPIRED            // Quote expired
}
