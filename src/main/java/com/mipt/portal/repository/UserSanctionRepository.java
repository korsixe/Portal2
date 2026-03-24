package com.mipt.portal.repository;

import com.mipt.portal.entity.UserSanction;
import com.mipt.portal.enums.SanctionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {
    List<UserSanction> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserSanction> findAllByUserIdAndTypeAndEndAtAfter(Long userId, SanctionType type, Instant now);
}
