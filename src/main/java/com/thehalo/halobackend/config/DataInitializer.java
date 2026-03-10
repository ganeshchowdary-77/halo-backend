package com.thehalo.halobackend.config;

import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.model.profile.AppRole;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Initialize default admin user if not exists
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                AppRole role = new AppRole();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void initializeAdminUser() {
        String adminEmail = "iamadmin@thehalo.com";
        String adminPassword = "admin123";
        
        Optional<AppUser> existingUser = userRepository.findByEmail(adminEmail);
        
        if (existingUser.isEmpty()) {
            AppRole iamAdminRole = roleRepository.findByName(RoleName.IAM_ADMIN)
                .orElseThrow(() -> new RuntimeException("IAM_ADMIN role not found"));

            AppUser adminUser = AppUser.builder()
                .email(adminEmail)
                .fullName("IAM Administrator")
                .firstName("IAM")
                .lastName("Administrator")
                .password(passwordEncoder.encode(adminPassword))
                .role(iamAdminRole)
                .build();

            userRepository.save(adminUser);
            log.info("Created default IAM admin user: {}", adminEmail);
        } else {
            // Update password to ensure it's correct
            AppUser user = existingUser.get();
            user.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(user);
            log.info("Updated IAM admin user password: {}", adminEmail);
        }
    }
}