package com.mipt.portal.repository;

import com.mipt.portal.entity.SupportRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
  List<SupportRequest> findByUserIdOrderByCreatedAtAsc(Long userId);
}

