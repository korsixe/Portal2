package com.mipt.portal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для запроса на управление монетами пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinManagementRequest {
    private Long targetUserId;
    private int amount;
    private String action; // "add" или "deduct"
    private String reason; // причина добавления/снятия монет
}
