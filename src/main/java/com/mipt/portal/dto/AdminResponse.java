package com.mipt.portal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для ответа на операции администрирования
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private boolean success;
    private String message;
    private Object data;

    public AdminResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }
}
