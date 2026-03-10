package com.thehalo.halobackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTester {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String plainPassword = "admin123";
        String hashedFromDB = "$2a$10$e0MYzXyjpJS7Pd0RVvHqHOxHbQZqBdkdmMfLZcyNONGKt4Dv3.3bO";
        
        System.out.println("Plain password: " + plainPassword);
        System.out.println("Hash from DB: " + hashedFromDB);
        System.out.println("Password matches: " + encoder.matches(plainPassword, hashedFromDB));
        
        // Generate a new hash for comparison
        String newHash = encoder.encode(plainPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(plainPassword, newHash));
    }
}