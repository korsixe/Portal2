package com.mipt.portal.service;

import com.mipt.portal.entity.SupportRequest;
import com.mipt.portal.repository.SupportRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupportRequestService {

  private final SupportRequestRepository supportRequestRepository;

  @Transactional(readOnly = true)
  public List<SupportRequest> getByUserId(Long userId) {
    return supportRequestRepository.findByUserIdOrderByCreatedAtAsc(userId);
  }

  @Transactional
  public SupportRequest create(Long userId, String userName, String message) {
    SupportRequest request = new SupportRequest();
    request.setUserId(userId);
    request.setUserName(userName);
    request.setMessage(message);
    request.setCreatedAt(LocalDateTime.now());
    return supportRequestRepository.save(request);
  }
}

