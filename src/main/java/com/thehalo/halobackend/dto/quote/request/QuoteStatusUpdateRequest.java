package com.thehalo.halobackend.dto.quote.request;

import com.thehalo.halobackend.enums.QuoteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private QuoteStatus status;
}
