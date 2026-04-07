package com.mipt.portal.dto;

import lombok.Data;

@Data
public class SanctionRequest {
    private Long targetUserId;
    private String reason;
    private Integer duration;
    private String type;
}
