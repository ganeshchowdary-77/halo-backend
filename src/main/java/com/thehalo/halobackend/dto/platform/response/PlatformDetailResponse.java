package com.thehalo.halobackend.dto.platform.response;

import com.thehalo.halobackend.enums.Niche;
import lombok.*;

import java.time.LocalDateTime;

// Full platform detail for admin management or influencer settings page
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformDetailResponse {

        private Long id;
        private String platformName;
        private String platformDescription;
        private String handle;
        private String platformUrl;
        private Long followerCount;
        private Double engagementRate;
        private Niche niche;
        private Boolean verified;
        // Whether this platform has an active policy
        private Boolean hasActivePolicy;
        // Underwriter-computed risk score (0-100)
        private Integer riskScore;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Document paths
        private String addressProofPath;
        private String incomeProofPath;
}
