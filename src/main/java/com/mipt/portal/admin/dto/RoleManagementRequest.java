package com.mipt.portal.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для запроса на назначение/отзыв роли
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleManagementRequest {
    private Long targetUserId;
    private String action; // "assign" или "revoke"
    private String role; // "MODERATOR" или "ADMIN"
    private String reason; // причина назначения/отзыва роли
}
