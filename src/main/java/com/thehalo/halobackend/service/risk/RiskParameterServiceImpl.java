package com.thehalo.halobackend.service.risk;

import com.thehalo.halobackend.dto.risk.request.CreateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.request.UpdateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.response.RiskParameterResponse;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.exception.business.DuplicateResourceException;
import com.thehalo.halobackend.model.RiskParameter;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskParameterServiceImpl implements RiskParameterService {

    private final RiskParameterRepository riskParameterRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public RiskParameterResponse createParameter(CreateRiskParameterRequest request, String createdByEmail) {
        // Check if parameter key already exists
        if (riskParameterRepository.existsByParamKey(request.getParamKey())) {
            throw new DuplicateResourceException("Risk parameter with key '" + request.getParamKey() + "' already exists");
        }

        // Find the user creating the parameter
        AppUser createdByUser = appUserRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + createdByEmail));

        // Create new parameter
        RiskParameter parameter = new RiskParameter();
        parameter.setParamKey(request.getParamKey());
        parameter.setLabel(request.getLabel());
        parameter.setMultiplier(request.getMultiplier());
        parameter.setDescription(request.getDescription());
        parameter.setApplicableNiche(request.getApplicableNiche());
        parameter.setActive(true);
        parameter.setEffectiveFrom(LocalDate.now());
        parameter.setUpdateNote(request.getUpdateNote());
        parameter.setUpdatedByUser(createdByUser);

        log.info("Creating new risk parameter {} with multiplier {} by user {}. Reason: {}", 
                request.getParamKey(), 
                request.getMultiplier(),
                createdByEmail,
                request.getUpdateNote());

        RiskParameter savedParameter = riskParameterRepository.save(parameter);
        return mapToResponse(savedParameter);
    }

    @Override
    public Page<RiskParameterResponse> getAllParameters(Pageable pageable) {
        return riskParameterRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public RiskParameterResponse getParameterById(Long id) {
        RiskParameter parameter = riskParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Risk parameter not found with id: " + id));
        
        return mapToResponse(parameter);
    }

    @Override
    @Transactional
    public RiskParameterResponse updateParameter(Long id, UpdateRiskParameterRequest request, String updatedByEmail) {
        // Find the parameter
        RiskParameter parameter = riskParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Risk parameter not found with id: " + id));

        // Find the user making the update
        AppUser updatedByUser = appUserRepository.findByEmail(updatedByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + updatedByEmail));

        // Log the change
        log.info("Updating risk parameter {} from {} to {} by user {}. Reason: {}", 
                parameter.getParamKey(), 
                parameter.getMultiplier(), 
                request.getMultiplier(),
                updatedByEmail,
                request.getUpdateNote());

        // Update the parameter
        parameter.setMultiplier(request.getMultiplier());
        parameter.setUpdateNote(request.getUpdateNote());
        parameter.setUpdatedByUser(updatedByUser);

        // Save and return
        RiskParameter savedParameter = riskParameterRepository.save(parameter);
        return mapToResponse(savedParameter);
    }

    private RiskParameterResponse mapToResponse(RiskParameter parameter) {
        RiskParameterResponse response = new RiskParameterResponse();
        response.setId(parameter.getId());
        response.setParamKey(parameter.getParamKey());
        response.setLabel(parameter.getLabel());
        response.setMultiplier(parameter.getMultiplier());
        response.setDescription(parameter.getDescription());
        response.setApplicableNiche(parameter.getApplicableNiche());
        response.setUpdateNote(parameter.getUpdateNote());
        response.setUpdatedAt(parameter.getUpdatedAt());
        response.setActive(parameter.getActive());
        
        if (parameter.getUpdatedByUser() != null) {
            response.setUpdatedByUserName(parameter.getUpdatedByUser().getFirstName() + " " + 
                                        parameter.getUpdatedByUser().getLastName());
        }
        
        return response;
    }
}