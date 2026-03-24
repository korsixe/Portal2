package com.mipt.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Сводная статистика по ролям и пользователям для отображения в админке/кабинете модератора.
 */
@Data
@AllArgsConstructor
public class SystemStats {
    private long totalUsers;
    private long adminCount;
    private long moderatorCount;
    private long regularUserCount;
}

