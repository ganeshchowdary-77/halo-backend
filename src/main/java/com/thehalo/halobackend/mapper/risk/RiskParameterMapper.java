package com.thehalo.halobackend.mapper.risk;

import com.thehalo.halobackend.dto.risk.response.RiskParameterResponse;
import com.thehalo.halobackend.model.underwriting.RiskParameter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskParameterMapper {

    @Mapping(source = "lastModifiedBy", target = "updatedByUserName")
    @Mapping(source = "lastModifiedDate", target = "updatedAt")
    @Mapping(target = "label", ignore = true)
    RiskParameterResponse toResponse(RiskParameter parameter);
}