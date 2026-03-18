package com.thehalo.halobackend.service.dashboard;

import java.util.Map;

public interface OfficerDashboardService {
    Map<String, Object> getOverview();
    Map<String, Object> getClaimsAnalytics();
}
