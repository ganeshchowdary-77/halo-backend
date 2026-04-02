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
    Product toEntity(CreateProductRequest request);

    // Public product response for landing page
    @Mapping(target = "coverageAmount", expression = "java(product.getCoverageAmount())")
    @Mapping(target = "coveredLegal", source = "coveredLegal")
    @Mapping(target = "coveredReputation", source = "coveredReputation")
    @Mapping(target = "coveredCyber", source = "coveredCyber")
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "popular", constant = "false")
    @Mapping(target = "marketingMessage", source = "tagline")
    com.thehalo.halobackend.dto.product.response.PublicProductResponse toPublicResponse(Product product);

    // Summary projection: rename coveredX -> coverageX to match DTO field names
    @Mapping(target = "coverageLegal", source = "coveredLegal")
    @Mapping(target = "coverageReputation", source = "coveredReputation")
    @Mapping(target = "coverageCyber", source = "coveredCyber")
    @Mapping(target = "keyFeatures", ignore = true)
    ProductSummaryResponse toSummary(Product product);

    // Detail projection: same renames + computed totalCoverageLimit
    @Mapping(target = "coverageLegal", source = "coveredLegal")
    @Mapping(target = "coverageReputation", source = "coveredReputation")
    @Mapping(target = "coverageCyber", source = "coveredCyber")
    @Mapping(target = "totalCoverageLimit", expression = "java(sumLimits(product))")
    @Mapping(target = "coverageAmount", expression = "java(sumLimits(product))")
    @Mapping(target = "activePolicyCount", ignore = true)
    @Mapping(target = "keyFeatures", ignore = true)
    ProductDetailResponse toDetail(Product product);

    // Null-safe partial update of an existing entity from a patch request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);

    // Utility: map list of products to summary list
    List<ProductSummaryResponse> toSummaryList(List<Product> products);

    // Helper: sum all three coverage sub-limits for totalCoverageLimit computation
    default BigDecimal sumLimits(Product p) {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal legal = p.getCoverageLimitLegal() != null ? p.getCoverageLimitLegal() : zero;
        BigDecimal rep = p.getCoverageLimitReputation() != null ? p.getCoverageLimitReputation() : zero;
        BigDecimal cyb = p.getCoverageLimitCyber() != null ? p.getCoverageLimitCyber() : zero;
        return legal.add(rep).add(cyb);
    }
}