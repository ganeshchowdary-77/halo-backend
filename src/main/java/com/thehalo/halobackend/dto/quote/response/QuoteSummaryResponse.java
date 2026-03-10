package com.thehalo.halobackend.dto.quote.response;

import com.thehalo.halobackend.enums.QuoteStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Minimal projection for Quote list views
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteSummaryResponse {

    private Long id;
    private String quoteNumber;
    private QuoteStatus status;
    private String productName;
    private String profileHandle;
    private BigDecimal offeredPremium;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
