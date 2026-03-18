package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    // Find requests submitted by a specific user
    List<QuoteRequest> findByUserId(Long userId);

    // Find all by status (for Underwriter queue)
    List<QuoteRequest> findByStatus(QuoteStatus status);

    // Find all by status (for Underwriter queue) (paginated)
    Page<QuoteRequest> findByStatus(QuoteStatus status, Pageable pageable);

    // Find all assigned and in review queue (if needed)
    List<QuoteRequest> findByAssignedUnderwriterId(Long underwriterId);

    // Find by quote number for lookup
    Optional<QuoteRequest> findByQuoteNumber(String quoteNumber);

    // Find unassigned quotes with specific status
    List<QuoteRequest> findByStatusAndAssignedUnderwriterIsNull(QuoteStatus status);

    // Find unassigned quotes for the queue (paginated)
    @Query("SELECT q FROM QuoteRequest q WHERE q.assignedUnderwriter IS NULL AND q.status = 'PENDING' ORDER BY q.createdAt ASC")
    Page<QuoteRequest> findUnassignedQuotes(Pageable pageable);

    // Find quotes assigned to specific underwriter (paginated)
    @Query("SELECT q FROM QuoteRequest q WHERE q.assignedUnderwriter.id = :underwriterId ORDER BY q.reviewedAt DESC")
    Page<QuoteRequest> findByAssignedUnderwriterId(@Param("underwriterId") Long underwriterId, Pageable pageable);

    // Find quotes assigned to specific underwriter and by status (paginated)
    Page<QuoteRequest> findByAssignedUnderwriterIdAndStatus(Long assignedUnderwriterId, QuoteStatus status, Pageable pageable);

    // Find urgent unassigned quotes (older than cutoff time)
    @Query("SELECT q FROM QuoteRequest q WHERE q.assignedUnderwriter IS NULL AND q.status = 'PENDING' AND q.createdAt < :cutoff ORDER BY q.createdAt ASC")
    List<QuoteRequest> findUrgentUnassignedQuotes(@Param("cutoff") LocalDateTime cutoff);

    // Find unassigned quotes older than specified time
    @Query("SELECT q FROM QuoteRequest q WHERE q.assignedUnderwriter IS NULL AND q.status = 'PENDING' AND q.createdAt < :cutoff")
    List<QuoteRequest> findUnassignedQuotesOlderThan(@Param("cutoff") LocalDateTime cutoff);

    // Count active quotes by underwriter
    @Query("SELECT COUNT(q) FROM QuoteRequest q WHERE q.assignedUnderwriter.id = :underwriterId AND q.status IN ('PENDING', 'IN_REVIEW')")
    int countActiveQuotesByUnderwriter(@Param("underwriterId") Long underwriterId);

    @Query("SELECT COUNT(q) FROM QuoteRequest q WHERE q.assignedUnderwriter.id = :underwriterId AND q.status = 'APPROVED' AND q.updatedAt >= :startOfDay AND q.updatedAt < :endOfDay")
    int countCompletedQuotesToday(@Param("underwriterId") Long underwriterId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Count completed quotes this week by underwriter
    @Query("SELECT COUNT(q) FROM QuoteRequest q WHERE q.assignedUnderwriter.id = :underwriterId AND q.status IN ('APPROVED', 'REJECTED') AND q.updatedAt >= :startOfWeek")
    int countCompletedQuotesThisWeek(@Param("underwriterId") Long underwriterId, @Param("startOfWeek") LocalDateTime startOfWeek);

    // Get average processing time for underwriter (returns minutes) - H2 compatible
    @Query("SELECT COALESCE(COUNT(q), 0) FROM QuoteRequest q WHERE q.assignedUnderwriter.id = :underwriterId AND q.status IN ('APPROVED', 'REJECTED') AND q.reviewedAt IS NOT NULL")
    int getAvgProcessingTimeMinutes(@Param("underwriterId") Long underwriterId);

    // Find high-value quotes (for priority handling)
    @Query("SELECT q FROM QuoteRequest q WHERE q.offeredPremium > :amount AND q.assignedUnderwriter IS NULL ORDER BY q.offeredPremium DESC")
    List<QuoteRequest> findHighValueUnassignedQuotes(@Param("amount") java.math.BigDecimal amount);

    // Find quotes by platform
    @Query("SELECT q FROM QuoteRequest q WHERE q.profile.platform.name = :platform ORDER BY q.createdAt DESC")
    Page<QuoteRequest> findByPlatform(@Param("platform") com.thehalo.halobackend.enums.PlatformName platform, Pageable pageable);

    // Find quotes requiring underwriter review (high risk)
    @Query("SELECT q FROM QuoteRequest q WHERE q.status = 'PENDING' AND (q.profile.niche IN ('FINANCE', 'CRYPTO') OR q.profile.followerCount > 1000000) ORDER BY q.createdAt ASC")
    Page<QuoteRequest> findQuotesRequiringReview(Pageable pageable);

    // Find quotes by user and status
    List<QuoteRequest> findByUserIdAndStatus(Long userId, QuoteStatus status);

    // Search across user email, quote number, or product name with optional status filtering (paginated)
    @Query("SELECT q FROM QuoteRequest q " +
           "LEFT JOIN q.user u " +
           "LEFT JOIN q.product p " +
           "WHERE (:status IS NULL OR q.status = :status) AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(q.quoteNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<QuoteRequest> findBySearchAndStatus(@Param("search") String search, @Param("status") QuoteStatus status, Pageable pageable);

    // Count quotes by status
    long countByStatus(QuoteStatus status);

    // Count quotes by status created after a specific date
    long countByStatusAndCreatedAtAfter(QuoteStatus status, LocalDateTime createdAt);
}
