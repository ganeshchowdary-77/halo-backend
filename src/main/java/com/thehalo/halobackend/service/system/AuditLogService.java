package com.thehalo.halobackend.service.system;

import com.thehalo.halobackend.dto.system.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    void logAction(String entityName, String entityId, String action, String details);

    List<AuditLogResponse> getRecentLogs();

    List<AuditLogResponse> getLogsByEntity(String entityName);
}
