package com.mipt.portal.dto;

import com.mipt.portal.entity.User;

import java.util.List;

public class AdminDashboardResponse {
    private final List<User> users;
    private final SystemStats stats;

    public AdminDashboardResponse(List<User> users, SystemStats stats) {
        this.users = users;
        this.stats = stats;
    }

    public List<User> getUsers() {
        return users;
    }

    public SystemStats getStats() {
        return stats;
    }
}

