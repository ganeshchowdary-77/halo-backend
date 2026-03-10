package com.thehalo.halobackend.dto.profile.response;

import com.thehalo.halobackend.enums.Niche;
import lombok.*;

import java.time.LocalDateTime;

// Full profile detail for admin management or influencer settings page
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDetailResponse {

        private Long id;
        private String platformName;
        private String platformDescription;
        private String handle;
        private String profileUrl;
        private Long followerCount;
        private Double engagementRate;
        private Niche niche;
        private Boolean verified;
        // Underwriter-computed risk score (0-100)
        private Integer riskScore;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
}
