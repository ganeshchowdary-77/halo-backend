package com.thehalo.halobackend.config;

import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.AppRole;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer — runs on every startup.
 *
 * Strategy:
 *  - Roles:            idempotent — create only if missing
 *  - IAM Admin:        create only on first boot, never overwrite
 *  - Policy Admin:     create only on first boot, never overwrite
 *
 * ALL passwords are encoded via Spring's PasswordEncoder at runtime.
 * No raw BCrypt strings in SQL — this is the ONLY source of truth for passwords.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder   passwordEncoder;

    // ─── Seed credentials (change before prod, store in Vault/Env) ─────────
    private static final String DEFAULT_PASSWORD    = "admin123";

    private static final String IAM_ADMIN_EMAIL       = "iamadmin@thehalo.com";
    private static final String POLICY_ADMIN_EMAIL    = "policy@thehalo.com";
    private static final String UNDERWRITER_EMAIL     = "underwriter@thehalo.com";
    private static final String CLAIMS_OFFICER_EMAIL  = "claims@thehalo.com";

    @Override
    public void run(String... args) {
        initializeRoles();
        seedUser(IAM_ADMIN_EMAIL,      "IAM Administrator", "IAM",        "Administrator", RoleName.IAM_ADMIN);
        seedUser(POLICY_ADMIN_EMAIL,   "Spoorthy",          "Spoorthy",   "Admin",         RoleName.POLICY_ADMIN);
        seedUser(UNDERWRITER_EMAIL,    "Sri Nayani",        "Sri",        "Nayani",        RoleName.UNDERWRITER);
        seedUser(CLAIMS_OFFICER_EMAIL, "Mani Kumar",        "Mani",       "Kumar",         RoleName.CLAIMS_OFFICER);
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    /**
     * Ensure every role enum value exists in DB. Safe across restarts.
     */
    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                AppRole role = new AppRole();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    /**
     * Idempotent user seed — ONLY creates the user on first boot.
     * If the email already exists, skips silently.
     * Password is always hashed via PasswordEncoder — never stored plain.
     */
    private void seedUser(String email, String fullName, String firstName,
                          String lastName, RoleName roleName) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        AppRole role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found after init: " + roleName));

        AppUser user = AppUser.builder()
                .email(email)
                .fullName(fullName)
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(role)
                .build();

        userRepository.save(user);
    }
}