package com.thehalo.halobackend.dto.profile.response;

import com.thehalo.halobackend.enums.Niche;
import lombok.*;

// Minimal DTO for linked-profile cards and policy purchase dropdowns
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileSummaryResponse {

        private Long id;
        // e.g. "INSTAGRAM"
        private String platformName;
        // e.g. "@jane_creates"
        private String handle;
        private Long followerCount;
        private Niche niche;
        // Whether The Halo has verified ownership of this profile
        private Boolean verified;
}
