package com.thehalo.halobackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuickAuthTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testIamAdminLogin() {
        String url = "http://localhost:" + port + "/api/v1/auth/login";
        
        String requestBody = """
            {
              "email": "iamadmin@thehalo.com",
              "password": "admin123"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ LOGIN SUCCESS");
            } else {
                System.out.println("❌ LOGIN FAILED");
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
    }
}