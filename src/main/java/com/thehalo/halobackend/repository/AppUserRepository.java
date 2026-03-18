package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.model.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @Query("SELECT u FROM AppUser u JOIN FETCH u.role WHERE u.email = :email")
    Optional<AppUser> findByEmailWithRole(@Param("email") String email);

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByRoleNameNot(RoleName roleName);

    Optional<AppUser> findByIdAndRoleNameNot(Long id, RoleName roleName);

    List<AppUser> findByRoleName(RoleName roleName);

    // Find active underwriters ordered by workload (least busy first)
    @Query("SELECT u FROM AppUser u LEFT JOIN QuoteRequest q ON q.assignedUnderwriter.id = u.id AND q.status IN ('PENDING', 'IN_REVIEW') WHERE u.role.name = 'UNDERWRITER' GROUP BY u.id ORDER BY COUNT(q.id) ASC")
    List<AppUser> findActiveUnderwritersOrderByWorkload();

    // Find underwriters with experience in specific platform
    @Query("SELECT DISTINCT u FROM AppUser u JOIN QuoteRequest q ON q.assignedUnderwriter.id = u.id JOIN q.profile p WHERE u.role.name = 'UNDERWRITER' AND p.platform.name = :platform")
    List<AppUser> findUnderwritersByPlatformExperience(@Param("platform") String platform);
}
