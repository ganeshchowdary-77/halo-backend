package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.RiskParameter;
import com.thehalo.halobackend.enums.Niche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskParameterRepository extends JpaRepository<RiskParameter, Long> {

    /**
     * Check if a parameter key already exists
     */
    boolean existsByParamKey(String paramKey);

    /**
     * Find risk parameter by key
     */
    Optional<RiskParameter> findByParamKeyAndActiveTrue(String paramKey);

    /**
     * Find all active risk parameters
     */
    List<RiskParameter> findByActiveTrueOrderByParamKey();

    /**
     * Find all active risk parameters with pagination
     */
    Page<RiskParameter> findByActiveTrue(Pageable pageable);

    /**
     * Find risk parameters for a specific niche
     */
    List<RiskParameter> findByApplicableNicheAndActiveTrueOrderByParamKey(Niche niche);

    /**
     * Find follower tier parameters that apply to a specific follower count
     */
    @Query("SELECT rp FROM RiskParameter rp WHERE rp.active = true " +
           "AND rp.paramKey LIKE 'FOLLOWER_TIER_%' " +
           "AND (rp.minFollowerCount IS NULL OR rp.minFollowerCount <= :followerCount) " +
           "AND (rp.maxFollowerCount IS NULL OR rp.maxFollowerCount >= :followerCount)")
    List<RiskParameter> findApplicableFollowerTierParameters(@Param("followerCount") Long followerCount);

    /**
     * Find platform-specific risk parameters
     */
    @Query("SELECT rp FROM RiskParameter rp WHERE rp.active = true " +
           "AND rp.paramKey LIKE 'PLATFORM_%'")
    List<RiskParameter> findPlatformParameters();

    /**
     * Find niche-specific risk parameters
     */
    @Query("SELECT rp FROM RiskParameter rp WHERE rp.active = true " +
           "AND rp.paramKey LIKE 'NICHE_%'")
    List<RiskParameter> findNicheParameters();
}