package com.mipt.portal.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class ModerationMessage {
    private Long id;
    private Long adId;
    private String moderatorEmail;
    private String action; // 'approve', 'reject', 'delete'
    private String reason;
    private LocalDateTime createdAt;
    private Boolean isRead;

    public ModerationMessage() {}

    public ModerationMessage(Long adId, String moderatorEmail, String action, String reason, Boolean isRead) {
        this.adId = adId;
        this.moderatorEmail = moderatorEmail;
        this.action = action;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
        this.isRead = isRead;
    }
}