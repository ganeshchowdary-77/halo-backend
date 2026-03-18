package com.thehalo.halobackend.dto.platform.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPlatformRequest {
    private String verificationNotes;
    private String addressProofPath;
    private String idVerificationPath;
    private Integer previousClaimsCount;
    private BigDecimal previousClaimsAmount;
}