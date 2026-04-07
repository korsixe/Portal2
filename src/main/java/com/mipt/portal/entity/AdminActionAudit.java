package com.mipt.portal.entity;

import com.mipt.portal.enums.AdminActionType;
import com.mipt.portal.enums.AuditTargetType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "admin_action_audit")
public class AdminActionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_email")
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AdminActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private AuditTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "details", length = 2048)
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
