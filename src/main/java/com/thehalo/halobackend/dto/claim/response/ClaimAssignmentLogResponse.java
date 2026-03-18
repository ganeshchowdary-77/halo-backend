package com.thehalo.halobackend.dto.claim.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimAssignmentLogResponse {
    private Long id;
    private String claimNumber;
    private String action; // ASSIGNED, APPROVED, DENIED, SUBMITTED
    private String officerName;
    private String officerEmail;
    private LocalDateTime timestamp;
    private String details;
    private String expenseType;
    private Double claimAmount;
    private Double approvedAmount;
    private String profileHandle;
    private String policyNumber;
}