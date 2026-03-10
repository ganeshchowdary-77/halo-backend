package com.thehalo.halobackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtStaffCreationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateStaffWithJwt() {
        // Step 1: Login to get JWT
        String loginUrl = "http://localhost:" + port + "/api/v1/auth/login";
        String loginBody = """
            {
              "email": "iamadmin@thehalo.com",
              "password": "admin123"
            }
            """;

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> loginRequest = new HttpEntity<>(loginBody, loginHeaders);

        try {
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(loginUrl, loginRequest, String.class);
            System.out.println("Login Status: " + loginResponse.getStatusCode());
            
            if (loginResponse.getStatusCode() != HttpStatus.OK) {
                System.out.println("❌ LOGIN FAILED");
                return;
            }

            // Extract token (simple string parsing)
            String responseBody = loginResponse.getBody();
            String token = responseBody.substring(
                responseBody.indexOf("\"accessToken\":\"") + 15,
                responseBody.indexOf("\",\"tokenType\"")
            );
            
            System.out.println("✅ Login successful");
            System.out.println("Token: " + token.substring(0, 30) + "...");

            // Step 2: Create staff using JWT
            String staffUrl = "http://localhost:" + port + "/api/v1/iam/staff";
            String staffBody = """
                {
                  "email": "testpolicy@thehalo.com",
                  "fullName": "Test Policy Admin",
                  "password": "secure123",
                  "role": "POLICY_ADMIN"
                }
                """;

            HttpHeaders staffHeaders = new HttpHeaders();
            staffHeaders.setContentType(MediaType.APPLICATION_JSON);
            staffHeaders.set("Authorization", "Bearer " + token);
            HttpEntity<String> staffRequest = new HttpEntity<>(staffBody, staffHeaders);

            ResponseEntity<String> staffResponse = restTemplate.postForEntity(staffUrl, staffRequest, String.class);
            System.out.println("Staff Creation Status: " + staffResponse.getStatusCode());
            System.out.println("Staff Response: " + staffResponse.getBody());
            
            if (staffResponse.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("✅ STAFF CREATION SUCCESS");
            } else {
                System.out.println("❌ STAFF CREATION FAILED");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}