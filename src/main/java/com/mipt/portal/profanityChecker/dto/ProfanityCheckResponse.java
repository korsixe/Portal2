package com.mipt.portal.profanityChecker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfanityCheckResponse {
  private boolean hasProfanity;
}
