package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.thehalo.halobackend.enums.ExpenseType;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Influencer's own claims
    @Query("SELECT c FROM Claim c " +
           "LEFT JOIN FETCH c.profile p " +
           "LEFT JOIN FETCH p.platform " +
           "LEFT JOIN FETCH c.policy pol " +
           "WHERE c.filedBy.id = :userId")
    List<Claim> findByFiledById(@Param("userId") Long userId);

    // Officer's claim queue — all claims in a given status
    @Query("SELECT c FROM Claim c " +
           "LEFT JOIN FETCH c.profile p " +
           "LEFT JOIN FETCH p.platform " +
           "LEFT JOIN FETCH c.policy pol " +
           "WHERE c.status = :status")
    List<Claim> findByStatus(@Param("status") ClaimStatus status);

    // Officer's assigned queue
    @Query("SELECT c FROM Claim c " +
           "LEFT JOIN FETCH c.profile p " +
           "LEFT JOIN FETCH p.platform " +
           "LEFT JOIN FETCH c.policy pol " +
           "WHERE c.assignedOfficer.id = :officerId")
    List<Claim> findByAssignedOfficerId(@Param("officerId") Long officerId);

    // Find by policy
    List<Claim> findByPolicyId(Long policyId);

    // Find by user's policies
    List<Claim> findByPolicyUserId(Long userId);

    // Find by user's policies and specific claim ID
    Optional<Claim> findByPolicyUserIdAndId(Long userId, Long id);

    // Find by expense type
    List<Claim> findByExpenseType(ExpenseType expenseType);

    // Find by creation date range
    List<Claim> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Count claims by status
    long countByStatus(ClaimStatus status);

    // Find claim with documents, profile, and platform eagerly loaded
    @Query("SELECT c FROM Claim c " +
           "LEFT JOIN FETCH c.documents " +
           "LEFT JOIN FETCH c.profile p " +
           "LEFT JOIN FETCH p.platform " +
           "LEFT JOIN FETCH c.policy pol " +
           "LEFT JOIN FETCH pol.product " +
           "WHERE c.id = :id")
    Optional<Claim> findByIdWithDocuments(@Param("id") Long id);

    // Search and filter claims (paginated)
    @Query("SELECT c FROM Claim c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(LOWER(c.claimNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.policy.policyNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.filedBy.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<Claim> findBySearchAndStatus(
            @Param("search") String search, 
            @Param("status") ClaimStatus status, 
            org.springframework.data.domain.Pageable pageable);

    // Count active claims by officer

    // Count active claims by officer
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.assignedOfficer.id = :officerId AND c.status IN :activeStatuses")
    int countActiveClaimsByOfficer(@Param("officerId") Long officerId, @Param("activeStatuses") List<ClaimStatus> activeStatuses);

    // Count completed claims today by officer
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.assignedOfficer.id = :officerId AND c.status IN :completedStatuses AND FUNCTION('DATE', c.updatedAt) = CURRENT_DATE")
    int countCompletedClaimsToday(@Param("officerId") Long officerId, @Param("completedStatuses") List<ClaimStatus> completedStatuses);

    // Count completed claims this week by officer
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.assignedOfficer.id = :officerId AND c.status IN :completedStatuses AND c.updatedAt >= :startOfWeek")
    int countCompletedClaimsThisWeek(@Param("officerId") Long officerId, @Param("completedStatuses") List<ClaimStatus> completedStatuses, @Param("startOfWeek") LocalDateTime startOfWeek);

    // Search claims by user's full name (first name or last name)
    @Query("SELECT c FROM Claim c " +
           "LEFT JOIN FETCH c.profile p " +
           "LEFT JOIN FETCH p.platform " +
           "LEFT JOIN FETCH c.policy pol " +
           "WHERE LOWER(c.filedBy.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.filedBy.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(CONCAT(c.filedBy.firstName, ' ', c.filedBy.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Claim> findByUserName(@Param("name") String name);
}
