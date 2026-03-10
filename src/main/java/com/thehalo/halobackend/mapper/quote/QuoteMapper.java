package com.thehalo.halobackend.mapper.quote;

import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuoteMapper {

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "profile.handle", target = "profileHandle")
    QuoteSummaryResponse toSummaryDto(QuoteRequest quoteRequest);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.tagline", target = "productTagline")
    @Mapping(source = "product.basePremium", target = "basePremium")
    @Mapping(source = "profile.handle", target = "profileHandle")
    @Mapping(source = "profile.platform.name", target = "platformName")
    @Mapping(source = "profile.followerCount", target = "followerCount")
    @Mapping(source = "profile.engagementRate", target = "engagementRate")
    @Mapping(source = "profile.riskScore", target = "riskScore")
    @Mapping(target = "assignedUnderwriterName", expression = "java(quoteRequest.getAssignedUnderwriter() != null ? quoteRequest.getAssignedUnderwriter().getFullName() : null)")
    QuoteDetailResponse toDetailDto(QuoteRequest quoteRequest);
}
