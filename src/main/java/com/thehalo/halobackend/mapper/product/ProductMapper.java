package com.thehalo.halobackend.mapper.product;

import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.request.UpdateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.model.policy.Product;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Map creation request to entity; id and active are ignored (set by JPA and
    // service layer)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "tagline", ignore = true)
    @Mapping(target = "maturityTermMonths", ignore = true)
    @Mapping(target = "latePaymentDailyInterestRate", ignore = true)
    @Mapping(target = "guaranteedMaturityBenefit", ignore = true)
    @Mapping(target = "surrenderValueMultiplier", ignore = true)
    Product toEntity(CreateProductRequest request);

    // Summary projection: rename coveredX -> coverageX to match DTO field names
    @Mapping(target = "coverageLegal", source = "coveredLegal")
    @Mapping(target = "coveragePR", source = "coveredPR")
    @Mapping(target = "coverageMonitoring", source = "coveredMonitoring")
    @Mapping(target = "keyFeatures", ignore = true)
    ProductSummaryResponse toSummary(Product product);

    // Detail projection: same renames + computed totalCoverageLimit
    @Mapping(target = "coverageLegal", source = "coveredLegal")
    @Mapping(target = "coveragePR", source = "coveredPR")
    @Mapping(target = "coverageMonitoring", source = "coveredMonitoring")
    @Mapping(target = "totalCoverageLimit", expression = "java(sumLimits(product))")
    @Mapping(target = "activePolicyCount", ignore = true)
    ProductDetailResponse toDetail(Product product);

    // Null-safe partial update of an existing entity from a patch request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tagline", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "maturityTermMonths", ignore = true)
    @Mapping(target = "latePaymentDailyInterestRate", ignore = true)
    @Mapping(target = "guaranteedMaturityBenefit", ignore = true)
    @Mapping(target = "surrenderValueMultiplier", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);

    // Utility: map list of products to summary list
    List<ProductSummaryResponse> toSummaryList(List<Product> products);

    // Helper: sum all three coverage sub-limits for totalCoverageLimit computation
    default BigDecimal sumLimits(Product p) {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal legal = p.getCoverageLimitLegal() != null ? p.getCoverageLimitLegal() : zero;
        BigDecimal pr = p.getCoverageLimitPR() != null ? p.getCoverageLimitPR() : zero;
        BigDecimal mon = p.getCoverageLimitMonitoring() != null ? p.getCoverageLimitMonitoring() : zero;
        return legal.add(pr).add(mon);
    }
}