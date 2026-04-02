package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PolicyApplication entity.
 */
public interface PolicyApplicationRepository extends JpaRepository<PolicyApplication, Long> {

    // Find applications by user
    List<PolicyApplication> findByUserId(Long userId);

    // Check for duplicate active application
    @Query("SELECT COUNT(a) FROM PolicyApplication a WHERE a.user.id = :userId AND a.profile.id = :profileId AND a.product.id = :productId AND a.status IN ('UNDER_REVIEW', 'PENDING_PAYMENT')")
    long countActiveApplicationsForProfileAndProduct(@Param("userId") Long userId, @Param("profileId") Long profileId, @Param("productId") Long productId);

    // Find by status
    List<PolicyApplication> findByStatus(PolicyStatus status);
    Page<PolicyApplication> findByStatus(PolicyStatus status, Pageable pageable);

    // Find by application number
    @Query("SELECT a FROM PolicyApplication a WHERE a.applicationNumber = :applicationNumber")
    Optional<PolicyApplication> findByApplicationNumber(@Param("applicationNumber") String applicationNumber);

    // Find applications assigned to a specific underwriter
    List<PolicyApplication> findByAssignedUnderwriterId(Long underwriterId);
    Page<PolicyApplication> findByAssignedUnderwriterIdAndStatus(Long underwriterId, PolicyStatus status, Pageable pageable);

    // Count by status
    long countByStatus(PolicyStatus status);

    // Count active applications for underwriter workload
    @Query("SELECT COUNT(a) FROM PolicyApplication a WHERE a.assignedUnderwriter.id = :underwriterId AND a.status = 'UNDER_REVIEW'")
    int countActiveApplicationsByUnderwriter(@Param("underwriterId") Long underwriterId);

    // Find all applications (admin, paginated with search)
    @Query("SELECT a FROM PolicyApplication a " +
           "LEFT JOIN a.user u " +
           "LEFT JOIN a.product p " +
           "WHERE (:status IS NULL OR a.status = :status) AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.applicationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PolicyApplication> findBySearchAndStatus(@Param("search") String search, @Param("status") PolicyStatus status, Pageable pageable);

    // Find by user and status
    List<PolicyApplication> findByUserIdAndStatus(Long userId, PolicyStatus status);

    // Find application by policy ID
    Optional<PolicyApplication> findByPolicyId(Long policyId);
}
