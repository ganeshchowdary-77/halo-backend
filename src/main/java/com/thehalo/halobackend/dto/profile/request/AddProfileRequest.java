package com.thehalo.halobackend.dto.profile.request;

import com.thehalo.halobackend.enums.Niche;
import jakarta.validation.constraints.*;
import lombok.*;

// Influencer links a new social media channel to their account
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProfileRequest {

        @NotNull(message = "Platform ID is required")
        private Long platformId;

        @NotBlank(message = "Handle is required, e.g. @jane_creates")
        @Size(min = 2, max = 60)
        private String handle;

        @Size(max = 2048)
        private String profileUrl;

        // Minimum 1000 followers required to qualify for coverage
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
