package com.thehalo.halobackend.dto.system.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String entityName;
    private String entityId;
    private String action;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;
}
