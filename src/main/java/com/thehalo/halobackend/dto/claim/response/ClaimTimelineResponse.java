package com.thehalo.halobackend.dto.claim.response;

import com.thehalo.halobackend.enums.ClaimStatus;
import lombok.*;

import java.time.LocalDateTime;

// Embedded in ClaimDetailResponse — single entry in claim audit trail
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimTimelineResponse {

        private ClaimStatus fromStatus;
        private ClaimStatus toStatus;
        private String note;
        // Email of the person who triggered the status change
        private String changedBy;
        private LocalDateTime changedAt;
}
