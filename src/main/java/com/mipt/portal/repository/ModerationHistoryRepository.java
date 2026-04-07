package com.mipt.portal.repository;

import com.mipt.portal.entity.ModerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModerationHistoryRepository extends JpaRepository<ModerationHistory, Long> {
    List<ModerationHistory> findAllByAdIdOrderByCreatedAtDesc(Long adId);
}
