package com.mipt.portal.service;

import com.mipt.portal.dto.kafka.KafkaEventPayloads;
import com.mipt.portal.entity.User;
import com.mipt.portal.entity.UserSanction;
import com.mipt.portal.enums.SanctionType;
import com.mipt.portal.repository.UserRepository;
import com.mipt.portal.repository.UserSanctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SanctionService {

    private final UserRepository userRepository;
    private final UserSanctionRepository sanctionRepository;
    private final KafkaMessageService kafkaMessageService;

    @Transactional
    public Optional<Boolean> freezeUser(Long actorId, Long userId, String reason, int hours) {
        return applySanction(actorId, userId, reason, hours, SanctionType.FREEZE);
    }

    @Transactional
    public Optional<Boolean> banUser(Long actorId, Long userId, String reason, int days) {
        return applySanction(actorId, userId, reason, days, SanctionType.BAN);
    }

    @Transactional
    public Optional<Boolean> liftSanctions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Optional.of(false);
        }
        user.setFrozenUntil(null);
        user.setFrozenReason(null);
        user.setBannedUntil(null);
        user.setBanReason(null);
        userRepository.save(user);
        kafkaMessageService.sendUserEvent(
            "user.sanction.lifted",
            String.valueOf(userId),
            new KafkaEventPayloads.UserSanctionLifted(userId)
        );
        return Optional.of(true);
    }

    private Optional<Boolean> applySanction(Long actorId, Long userId, String reason, int duration, SanctionType type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Optional.of(false);
        }

        Instant start = Instant.now();
        Instant end = type == SanctionType.FREEZE
                ? start.plus(duration, ChronoUnit.HOURS)
                : start.plus(duration, ChronoUnit.DAYS);

        UserSanction sanction = new UserSanction();
        sanction.setUserId(userId);
        sanction.setType(type);
        sanction.setReason(reason);
        sanction.setStartAt(start);
        sanction.setEndAt(end);
        sanction.setCreatedBy(actorId);
        sanctionRepository.save(sanction);

        if (type == SanctionType.FREEZE) {
            user.setFrozenUntil(end);
            user.setFrozenReason(reason);
        } else {
            user.setBannedUntil(end);
            user.setBanReason(reason);
        }

        userRepository.save(user);
        kafkaMessageService.sendUserEvent(
            "user.sanction.applied",
            String.valueOf(userId),
            new KafkaEventPayloads.UserSanctionApplied(
                userId,
                actorId,
                type.name(),
                duration,
                end.toString(),
                (reason != null && !reason.isBlank()) ? reason : null
            )
        );
        return Optional.of(true);
    }
}
