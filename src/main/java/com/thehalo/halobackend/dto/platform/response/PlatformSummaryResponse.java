package com.thehalo.halobackend.dto.platform.response;

import com.thehalo.halobackend.enums.Niche;
import lombok.*;

// Minimal DTO for linked-platform cards and policy purchase dropdowns
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSummaryResponse {

        private Long id;
        // e.g. "INSTAGRAM"
        private String platformName;
        // e.g. "@jane_creates"
        private String handle;
        private Long followerCount;
        private Niche niche;
        // Whether The Halo has verified ownership of this platform
        private Boolean verified;
        // Whether this platform has an active policy
        private Boolean hasActivePolicy;

        // Influencer info for Admin verification views
        private String influencerName;
        private String influencerEmail;

        private Double engagementRate;
        private String verificationStatus;
        private String riskLevel;

        // Document paths for Admin review
        private String addressProofPath;
        private String incomeProofPath;
        
        // Document URLs for viewing
        private String addressProofUrl;
        private String incomeProofUrl;
        
        // Platform URL for profile viewing
        private String platformUrl;
        
        // Granular verification fields
        private Boolean nicheVerified;
        private Boolean addressVerified;
        private Boolean incomeVerified;
        
        // Granular rejection reasons
        private String nicheRejectionReason;
        private String addressRejectionReason;
        private String incomeRejectionReason;
}
