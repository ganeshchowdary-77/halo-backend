package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.SubmitPolicyApplicationRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;

import java.util.List;

public interface PolicyApplicationService {

    /**
     * Submit a policy application. Automatically calculates risk and premium.
     * Low-risk applications are auto-approved (creates Policy with PENDING_PAYMENT).
     * High-risk applications are auto-assigned to an underwriter for review.
     */
    PolicyApplicationDetailResponse submitApplication(SubmitPolicyApplicationRequest request);

    /**
     * Get all applications for the currently authenticated user.
     */
    List<PolicyApplicationDetailResponse> getMyApplications();

    /**
     * Get a single application detail by ID.
     */
    PolicyApplicationDetailResponse getApplicationDetail(Long applicationId);

    /**
     * Get applications assigned to a specific underwriter.
     */
    List<PolicyApplicationDetailResponse> getAssignedApplications(Long underwriterId);

    /**
     * Underwriter approves a high-risk application → creates Policy(PENDING_PAYMENT).
     */
    PolicyApplicationDetailResponse approveApplication(Long applicationId, String underwriterNotes);

    /**
     * Underwriter rejects a high-risk application.
     */
    PolicyApplicationDetailResponse rejectApplication(Long applicationId, String reason);
}
