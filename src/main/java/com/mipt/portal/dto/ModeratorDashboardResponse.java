package com.mipt.portal.dto;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.User;

import java.util.List;

public class ModeratorDashboardResponse {
    private final List<Announcement> ads;
    private final SystemStats stats;
    private final User moderator;

    public ModeratorDashboardResponse(List<Announcement> ads, SystemStats stats, User moderator) {
        this.ads = ads;
        this.stats = stats;
        this.moderator = moderator;
    }

    public List<Announcement> getAds() {
        return ads;
    }

    public SystemStats getStats() {
        return stats;
    }

    public User getModerator() {
        return moderator;
    }
}

