package com.mipt.portal.service;

import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.ModerationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModerationHistoryService {

    private final ModerationHistoryRepository moderationHistoryRepository;

    @Transactional
    public void record(Long adId, Long moderatorId, AdStatus fromStatus, AdStatus toStatus, String reason) {
        ModerationHistory history = new ModerationHistory();
        history.setAdId(adId);
        history.setModeratorId(moderatorId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        moderationHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<ModerationHistory> getHistory(Long adId) {
        return moderationHistoryRepository.findAllByAdIdOrderByCreatedAtDesc(adId);
    }
}
