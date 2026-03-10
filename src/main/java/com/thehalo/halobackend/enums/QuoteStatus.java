package com.thehalo.halobackend.enums;

public enum QuoteStatus {
    PENDING,
    REVIEWING,
    IN_REVIEW, // Being reviewed by underwriter
    APPROVED, // Underwriter approved and offered a premium
    REJECTED, // Underwriter rejected the request
    ACCEPTED, // Influencer accepted the quote and purchased
    EXPIRED // Time elapsed
}
