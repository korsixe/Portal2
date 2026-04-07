package com.mipt.portal.entity;

import com.mipt.portal.enums.AdStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "moderation_history")
public class ModerationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad_id", nullable = false)
    private Long adId;

    @Column(name = "moderator_id", nullable = false)
    private Long moderatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private AdStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private AdStatus toStatus;

    @Column(name = "reason", length = 1024)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
