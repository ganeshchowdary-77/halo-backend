package com.thehalo.halobackend.service.dashboard;

import java.util.Map;

public interface InfluencerDashboardService {
    Map<String, Object> getDashboardOverview();
    Map<String, Object> getUpcomingPaymentDues();
    Map<String, Object> getActivePoliciesSummary();
}
