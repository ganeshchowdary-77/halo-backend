package com.thehalo.halobackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for seeding database
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // Verify the hash works
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash verification: " + matches);
        
        // Test the existing hash from data.sql
        String existingHash = "$2a$10$e0MYzXyjpJS7Pd0RVvHqHOxHbQZqBdkdmMfLZcyNONGKt4Dv3.3bO";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash verification: " + existingMatches);
    }
}