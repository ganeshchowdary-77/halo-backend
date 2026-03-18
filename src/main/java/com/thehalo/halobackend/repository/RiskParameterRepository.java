package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.underwriting.RiskParameter;
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
     * Find risk parameter by key (only active)
     */
    Optional<RiskParameter> findByParamKeyAndActiveTrue(String paramKey);

    /**
     * Find risk parameter by key (including inactive)
     */
    Optional<RiskParameter> findByParamKey(String paramKey);

    /**
     * Find all active risk parameters
     */
    List<RiskParameter> findByActiveTrueOrderByParamKey();

    /**
     * Find all risk parameters (including inactive)
     */
    List<RiskParameter> findAllByOrderByParamKey();

    /**
     * Search risk parameters by param key or description
     */
    @Query("SELECT r FROM RiskParameter r WHERE " +
           "LOWER(r.paramKey) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY r.paramKey")
    Page<RiskParameter> findBySearchTerm(@Param("search") String search, Pageable pageable);
}