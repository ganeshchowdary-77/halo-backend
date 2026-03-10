package com.thehalo.halobackend.model.system;

import com.thehalo.halobackend.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE audit_logs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g., "PRODUCT", "POLICY", "IAM", "CLAIM"
    @Column(nullable = false, length = 50)
    private String entityName;

    // ID of the entity that was affected
    private String entityId;

    // e.g., "CREATE", "UPDATE", "DELETE", "APPROVE", "DENY"
    @Column(nullable = false, length = 50)
    private String action;

    // JSON or plain text description of the changes
    @Column(length = 2000)
    private String details;

    // Email or ID of the person who performed the action
    @Column(nullable = false, length = 100)
    private String performedBy;
}
