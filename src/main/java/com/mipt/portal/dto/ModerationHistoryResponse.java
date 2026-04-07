package com.mipt.portal.dto;

import com.mipt.portal.entity.AdminActionAudit;
import com.mipt.portal.entity.ModerationHistory;

import java.util.List;

public class ModerationHistoryResponse {
    private final List<ModerationHistory> history;
    private final List<AdminActionAudit> adminActions;

    public ModerationHistoryResponse(List<ModerationHistory> history, List<AdminActionAudit> adminActions) {
        this.history = history;
        this.adminActions = adminActions;
    }

    public List<ModerationHistory> getHistory() {
        return history;
    }

    public List<AdminActionAudit> getAdminActions() {
        return adminActions;
    }
}

