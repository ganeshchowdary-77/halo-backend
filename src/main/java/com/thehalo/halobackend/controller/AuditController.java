package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.system.response.AuditLogResponse;
import com.thehalo.halobackend.service.system.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/audit-logs")
@RequiredArgsConstructor
@Tag(name = "System Ledger (Audit Logs)", description = "Audit logging endpoints for tracking system actions")
public class AuditController {

    private final AuditLogService auditLogService;

    // Both IAM_ADMIN (sees security/auth logs) and POLICY_ADMIN (sees
    // product/business logs) get access
    @GetMapping
    @PreAuthorize("hasAnyRole('IAM_ADMIN', 'POLICY_ADMIN')")
    @Operation(summary = "Get audit logs", description = "Retrieves system audit logs. Can optionally filter by entity name.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires IAM_ADMIN or POLICY_ADMIN role")
    })
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getLogs(
            @RequestParam(required = false) String entityName) {

        List<AuditLogResponse> logs;
        if (entityName != null && !entityName.isBlank()) {
            logs = auditLogService.getLogsByEntity(entityName);
        } else {
            logs = auditLogService.getRecentLogs();
        }

        return ResponseFactory.success(logs, "Audit logs retrieved successfully");
    }
}
