package com.thehalo.halobackend.dto.platform.request;

import com.thehalo.halobackend.enums.Niche;
import jakarta.validation.constraints.*;
import lombok.*;

// Influencer updates an existing social media platform
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlatformRequest {

        @Size(max = 2048)
        private String platformUrl;

        @NotNull
        @Min(value = 1000, message = "Minimum 1,000 followers required")
        private Long followerCount;

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        private Double engagementRate;

        @NotNull(message = "Content niche is required")
        private Niche niche;
}
