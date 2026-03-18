package com.thehalo.halobackend.exception.codes;

public enum ErrorCode {

    // ── Validation ─────────────────────────────────────────────────
    VALIDATION_ERROR(400),

    // ── Generic Business ───────────────────────────────────────────
    RESOURCE_NOT_FOUND(404),
    DUPLICATE_RESOURCE(409),
    BUSINESS_RULE_VIOLATION(422),

    // ── Auth Domain ────────────────────────────────────────────────
    USER_NOT_FOUND(404),
    EMAIL_ALREADY_REGISTERED(409),
    ROLE_NOT_FOUND(404),

    // ── Profile Domain ─────────────────────────────────────────────
    PROFILE_NOT_FOUND(404),
    PLATFORM_HANDLE_ALREADY_LINKED(409),
    PLATFORM_NOT_SUPPORTED(422),
    PROFILE_NOT_VERIFIED(403),

    // ── Payment Domain ─────────────────────────────────────────────
    UNAUTHORIZED_ACCESS(403),
    INVALID_PAYMENT_STATE(400),
    INSUFFICIENT_PAYMENT(400),
    PAYMENT_PROCESSING_FAILED(400),

    // ── Policy Domain ──────────────────────────────────────────────
    POLICY_NOT_FOUND(404),
    POLICY_NOT_ACTIVE(422),
    POLICY_ALREADY_EXPIRED(422),
    DUPLICATE_ACTIVE_POLICY(409), // Profile already has active policy for this product
    PRODUCT_NOT_FOUND(404),
    PRODUCT_NOT_AVAILABLE(422),

    // ── Claim Domain ───────────────────────────────────────────────
    CLAIM_NOT_FOUND(404),
    CLAIM_NOT_MODIFIABLE(422), // Claim not in PENDING state, cannot be edited
    CLAIM_AMOUNT_EXCEEDS_COVERAGE(422), // claimAmount > policy coverage limit
    CLAIM_ALREADY_REVIEWED(409), // Officer tries to review an already closed claim
    INVALID_EXPENSE_TYPE(400),

    // ── Underwriter Domain ─────────────────────────────────────────
    RISK_PARAMETER_NOT_FOUND(404),
    RISK_SCORE_INVALID(400), // Score outside allowed range

    // ── Security ───────────────────────────────────────────────────
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INVALID_CREDENTIALS(401),
    TOKEN_EXPIRED(401),
    INSUFFICIENT_ROLE(403),
    REFRESH_TOKEN_EXPIRED(401),
    INVALID_REFRESH_TOKEN(401),
    RATE_LIMIT_EXCEEDED(429),

    // ── System ─────────────────────────────────────────────────────
    DATABASE_ERROR(500),
    EXTERNAL_SERVICE_ERROR(503),
    FILE_STORAGE_ERROR(500),
    INVALID_FILE_NAME(400),
    INTERNAL_SERVER_ERROR(500);

    private final int status;

    ErrorCode(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
