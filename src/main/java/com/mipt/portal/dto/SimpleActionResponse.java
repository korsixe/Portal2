package com.mipt.portal.dto;

public class SimpleActionResponse {
    private final boolean success;
    private final String message;

    public SimpleActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

