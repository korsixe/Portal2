package com.mipt.portal.service;

import com.mipt.portal.dto.kafka.KafkaEventPayloads;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.ModerationHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationHistoryService {

    private final ModerationHistoryRepository moderationHistoryRepository;
    private final KafkaMessageService kafkaMessageService;

    @Transactional
    public void record(Long adId, Long moderatorId, AdStatus fromStatus, AdStatus toStatus, String reason) {
        ModerationHistory history = new ModerationHistory();
        history.setAdId(adId);
        history.setModeratorId(moderatorId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        ModerationHistory saved = moderationHistoryRepository.save(history);
        kafkaMessageService.sendModerationEvent(
            "moderation.history.recorded",
            String.valueOf(saved.getId()),
            new KafkaEventPayloads.ModerationHistoryRecorded(
                saved.getId(),
                adId,
                moderatorId,
                fromStatus.name(),
                toStatus.name(),
                (reason != null && !reason.isBlank()) ? reason : null
            )
        );
    }

    @Transactional(readOnly = true)
    public List<ModerationHistory> getHistory(Long adId) {
        return moderationHistoryRepository.findAllByAdIdOrderByCreatedAtDesc(adId);
    }
}
