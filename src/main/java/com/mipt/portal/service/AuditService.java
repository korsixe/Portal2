package com.mipt.portal.service;

import com.mipt.portal.entity.AdminActionAudit;
import com.mipt.portal.entity.AdminLoginAudit;
import com.mipt.portal.enums.AdminActionType;
import com.mipt.portal.enums.AuditTargetType;
import com.mipt.portal.repository.AdminActionAuditRepository;
import com.mipt.portal.repository.AdminLoginAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AdminActionAuditRepository adminActionAuditRepository;
    private final AdminLoginAuditRepository adminLoginAuditRepository;

    @Transactional
    public void logAdminAction(Long actorId,
                               String actorEmail,
                               AdminActionType actionType,
                               AuditTargetType targetType,
                               Long targetId,
                               String details) {
        AdminActionAudit audit = new AdminActionAudit();
        audit.setActorId(actorId);
        audit.setActorEmail(actorEmail);
        audit.setActionType(actionType);
        audit.setTargetType(targetType);
        audit.setTargetId(targetId);
        audit.setDetails(details);
        adminActionAuditRepository.save(audit);
    }

    @Transactional
    public void logAdminLogin(String email, boolean success, String ip, String userAgent) {
        AdminLoginAudit audit = new AdminLoginAudit();
        audit.setAdminEmail(email);
        audit.setSuccess(success);
        audit.setIp(ip);
        audit.setUserAgent(userAgent);
        adminLoginAuditRepository.save(audit);
    }
}
