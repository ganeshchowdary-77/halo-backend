package com.thehalo.halobackend.enums;

/**
 * The three coverage pillars offered by Halo insurance products.
 * Maps directly to the Product entity coverage flags:
 *   LEGAL       → coveredLegal / coverageLimitLegal
 *   REPUTATION  → coveredReputation / coverageLimitReputation
 *   CYBER       → coveredCyber / coverageLimitCyber
 */
public enum ExpenseType {
    LEGAL,
    REPUTATION,
    CYBER
}
