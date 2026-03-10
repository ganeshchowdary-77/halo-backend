package com.thehalo.halobackend.dto.iam.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaffSummaryResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private Boolean active;
}