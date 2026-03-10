package com.thehalo.halobackend.service.risk;

import com.thehalo.halobackend.dto.risk.request.CreateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.request.UpdateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.response.RiskParameterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiskParameterService {
    
    /**
     * Create a new risk parameter
     */
    RiskParameterResponse createParameter(CreateRiskParameterRequest request, String createdByEmail);
    
    /**
     * Get all risk parameters with pagination
     */
    Page<RiskParameterResponse> getAllParameters(Pageable pageable);
    
    /**
     * Get a specific risk parameter by ID
     */
    RiskParameterResponse getParameterById(Long id);
    
    /**
     * Update a risk parameter with audit trail
     */
    RiskParameterResponse updateParameter(Long id, UpdateRiskParameterRequest request, String updatedByEmail);
}