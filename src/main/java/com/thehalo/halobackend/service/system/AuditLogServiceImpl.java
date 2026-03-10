package com.thehalo.halobackend.service.system;

import com.thehalo.halobackend.dto.system.response.AuditLogResponse;
import com.thehalo.halobackend.model.system.AuditLog;
import com.thehalo.halobackend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(String entityName, String entityId, String action, String details) {
        String performedBy = "system";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            performedBy = auth.getName(); // Usually the email for JWT
        }

        AuditLog log = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .details(details)
                .performedBy(performedBy)
                .build();

        // Save independently so failures don't necessarily rollback the main
        // transaction
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByEntity(String entityName) {
        return auditLogRepository.findByEntityNameOrderByCreatedAtDesc(entityName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .details(log.getDetails())
                .performedBy(log.getPerformedBy())
                .timestamp(log.getCreatedAt())
                .build();
    }
}
