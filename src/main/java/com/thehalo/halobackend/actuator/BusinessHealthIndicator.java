package com.thehalo.halobackend.actuator;

import com.thehalo.halobackend.repository.ClaimRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class BusinessHealthIndicator implements HealthIndicator {
    
    private final ClaimRepository claimRepository;
    
    public BusinessHealthIndicator(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }
    
    @Override
    public Health health() {
        try {
            long totalClaims = claimRepository.count();
            
            return Health.up()
                .withDetail("totalClaims", totalClaims)
                .withDetail("status", "Operational")
                .withDetail("message", "Basic business metrics are accessible")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Unable to fetch business metrics")
                .withDetail("errorMessage", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
