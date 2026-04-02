package com.thehalo.halobackend.service.underwriting;

import com.thehalo.halobackend.dto.risk.request.CreateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.request.UpdateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.response.RiskParameterResponse;
import com.thehalo.halobackend.exception.business.BusinessException;
import com.thehalo.halobackend.exception.codes.ErrorCode;
import com.thehalo.halobackend.exception.domain.underwriter.RiskParameterNotFoundException;
import com.thehalo.halobackend.exception.business.DuplicateResourceException;
import com.thehalo.halobackend.mapper.risk.RiskParameterMapper;
import com.thehalo.halobackend.model.underwriting.RiskParameter;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RiskParameterServiceImpl implements RiskParameterService {

    private final RiskParameterRepository riskParameterRepository;
    private final AppUserRepository appUserRepository;
    private final RiskParameterMapper riskParameterMapper;

    @Override
    @Transactional
    public RiskParameterResponse createParameter(CreateRiskParameterRequest request, String createdByEmail) {
        // Check if parameter key already exists
        if (riskParameterRepository.existsByParamKey(request.getParamKey())) {
            throw new DuplicateResourceException("Risk parameter with key '" + request.getParamKey() + "' already exists");
        }

        // Find the user creating the parameter
        AppUser createdByUser = appUserRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new BusinessException("User not found with email: " + createdByEmail, ErrorCode.RESOURCE_NOT_FOUND));

        // Create new parameter
        RiskParameter parameter = new RiskParameter();
        parameter.setParamKey(request.getParamKey());
        parameter.setMultiplier(request.getMultiplier());
        parameter.setDescription(request.getDescription());
        parameter.setActive(true);
        parameter.setLastModifiedBy(createdByUser.getEmail());
        parameter.setLastModifiedDate(LocalDateTime.now());
        parameter.setUpdateNote(request.getUpdateNote());

        RiskParameter savedParameter = riskParameterRepository.save(parameter);
        return riskParameterMapper.toResponse(savedParameter);
    }

    @Override
    public Page<RiskParameterResponse> getAllParameters(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return riskParameterRepository.findAll(pageable)
                    .map(riskParameterMapper::toResponse);
        } else {
            return riskParameterRepository.findBySearchTerm(search.trim(), pageable)
                    .map(riskParameterMapper::toResponse);
        }
    }

    @Override
    public RiskParameterResponse getParameterById(Long id) {
        RiskParameter parameter = riskParameterRepository.findById(id)
                .orElseThrow(() -> new RiskParameterNotFoundException(id));
        
        return riskParameterMapper.toResponse(parameter);
    }

    @Override
    @Transactional
    public RiskParameterResponse updateParameter(Long id, UpdateRiskParameterRequest request, String updatedByEmail) {
        // Find the parameter
        RiskParameter parameter = riskParameterRepository.findById(id)
                .orElseThrow(() -> new RiskParameterNotFoundException(id));

        // Find the user making the update
        AppUser updatedByUser = appUserRepository.findByEmail(updatedByEmail)
                .orElseThrow(() -> new BusinessException("User not found with email: " + updatedByEmail, ErrorCode.RESOURCE_NOT_FOUND));

        parameter.setMultiplier(request.getMultiplier());
        parameter.setUpdateNote(request.getUpdateNote());
        parameter.setLastModifiedBy(updatedByEmail);
        parameter.setLastModifiedDate(LocalDateTime.now());

        // Save and return
        RiskParameter savedParameter = riskParameterRepository.save(parameter);
        return riskParameterMapper.toResponse(savedParameter);
    }
}
