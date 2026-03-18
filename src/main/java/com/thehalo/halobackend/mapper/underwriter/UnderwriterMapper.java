package com.thehalo.halobackend.mapper.underwriter;

import com.thehalo.halobackend.dto.underwriter.response.QueueItemResponse;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnderwriterMapper {

    @Mapping(source = "id", target = "quoteId")
    @Mapping(source = "quoteNumber", target = "quoteNumber")
    @Mapping(source = "user.fullName", target = "influencerName")
    @Mapping(source = "user.email", target = "influencerEmail")
    @Mapping(source = "profile.platform.name", target = "platform")
    @Mapping(source = "profile.niche", target = "niche")
    @Mapping(source = "profile.followerCount", target = "followerCount")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "requestedCoverage", expression = "java(calculateTotalCoverage(quote.getProduct()))")
    @Mapping(target = "estimatedPremium", expression = "java(quote.getOfferedPremium() != null ? quote.getOfferedPremium() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "timeInQueue", expression = "java(java.time.Duration.between(quote.getCreatedAt(), java.time.LocalDateTime.now()).toMinutes())")
    @Mapping(target = "assignedUnderwriter", expression = "java(quote.getAssignedUnderwriter() != null ? quote.getAssignedUnderwriter().getFullName() : null)")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "priority", ignore = true)
    QueueItemResponse toQueueItem(QuoteRequest quote);

    default java.math.BigDecimal calculateTotalCoverage(com.thehalo.halobackend.model.policy.Product product) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        if (product.getCoverageLimitLegal() != null) total = total.add(product.getCoverageLimitLegal());
        if (product.getCoverageLimitPR() != null) total = total.add(product.getCoverageLimitPR());
        if (product.getCoverageLimitMonitoring() != null) total = total.add(product.getCoverageLimitMonitoring());
        return total;
    }
}