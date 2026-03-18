package com.thehalo.halobackend.service.dashboard;

import java.util.Map;

public interface UnderwriterDashboardService {
    Map<String, Object> getOverview();
    Map<String, Object> getPremiumCalculations(int page, int size);
    Map<String, Object> getPremiumCalculationDetail(Long quoteId);
}
