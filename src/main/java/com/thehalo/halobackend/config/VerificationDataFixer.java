package com.thehalo.halobackend.config;

import com.thehalo.halobackend.repository.UserPlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time data fixer to update verification fields from false to null
 * This fixes platforms that were created with @Builder.Default = false
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationDataFixer implements CommandLineRunner {

    private final UserPlatformRepository platformRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking for platforms with incorrect verification defaults...");
        
        // Find platforms where all verification fields are false but platform is not verified
        var platforms = platformRepository.findByVerifiedFalse();
        
        int fixed = 0;
        for (var platform : platforms) {
            // Only fix if all three are explicitly false (not null)
            if (Boolean.FALSE.equals(platform.getNicheVerified()) &&
                Boolean.FALSE.equals(platform.getAddressVerified()) &&
                Boolean.FALSE.equals(platform.getIncomeVerified())) {
                
                platform.setNicheVerified(null);
                platform.setAddressVerified(null);
                platform.setIncomeVerified(null);
                platformRepository.save(platform);
                fixed++;
                
                log.info("Fixed verification fields for platform ID: {} ({})", 
                    platform.getId(), platform.getHandle());
            }
        }
        
        if (fixed > 0) {
            log.info("Fixed {} platform(s) with incorrect verification defaults", fixed);
        } else {
            log.info("No platforms needed fixing");
        }
    }
}
