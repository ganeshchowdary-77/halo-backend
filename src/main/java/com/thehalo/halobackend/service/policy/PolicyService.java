package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;

import java.util.List;

public interface PolicyService {
        List<PolicySummaryResponse> getMyPolicies();

        PolicyDetailResponse getDetail(Long policyId);

        PolicyDetailResponse purchase(PurchasePolicyRequest request);

        PolicyDetailResponse purchaseFromQuote(Long quoteId);

        PolicyDetailResponse payPremium(Long policyId);

        PolicySummaryResponse cancel(Long policyId);

        List<PolicySummaryResponse> getAllPolicies();

        List<com.thehalo.halobackend.dto.policy.response.PolicyApplicationResponse> getAdminApplications();

        void approvePolicyApplication(Long id);

        void rejectPolicyApplication(Long id, String reason);


        // Helper methods for navigation visibility
        boolean hasActivePolicies();

        boolean hasPaidPremium();

}
