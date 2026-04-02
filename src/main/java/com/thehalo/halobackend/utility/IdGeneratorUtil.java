package com.thehalo.halobackend.utility;

import java.time.Year;
import java.util.UUID;

/**
 * Enterprise Utility class for generating standardized business identifiers.
 * Designed to be globally accessible, stateless, and easily mockable for unit
 * testing.
 */
public final class IdGeneratorUtil {

    private IdGeneratorUtil() {
        // Prevent instantiation of utility class
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a unique Policy Number (e.g., POL-2024-8A3B)
     */
    public static String generatePolicyNumber() {
        return "POL-" + Year.now().getValue() + "-" + generateShortCode();
    }

    /**
     * Generates a unique Claim Number (e.g., CLM-2024-9C4D)
     */
    public static String generateClaimNumber() {
        return "CLM-" + Year.now().getValue() + "-" + generateShortCode();
    }

    /**
     * Generates a unique Policy Application Number (e.g., APP-2024-1X2Y)
     */
    public static String generateApplicationNumber() {
        return "APP-" + Year.now().getValue() + "-" + generateShortCode();
    }

    /**
     * Generates a short 6-character alphanumeric code for readable IDs
     */
    private static String generateShortCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
