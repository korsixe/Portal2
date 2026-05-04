package com.mipt.portal.service;

import com.mipt.portal.entity.AdminActionAudit;
import com.mipt.portal.entity.AdminLoginAudit;
import com.mipt.portal.enums.AdminActionType;
import com.mipt.portal.enums.AuditTargetType;
import com.mipt.portal.repository.AdminActionAuditRepository;
import com.mipt.portal.repository.AdminLoginAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

  @Mock
  private AdminActionAuditRepository actionRepo;
  @Mock
  private AdminLoginAuditRepository loginRepo;
  @InjectMocks
  private AuditService auditService;

  @Test
  void logAdminAction_persistsCorrectFields() {
    auditService.logAdminAction(
        7L, "admin@phystech.edu",
        AdminActionType.ROLE_CHANGE, AuditTargetType.USER,
        42L, "Назначен модератор");

    ArgumentCaptor<AdminActionAudit> captor = ArgumentCaptor.forClass(AdminActionAudit.class);
    verify(actionRepo).save(captor.capture());
    AdminActionAudit saved = captor.getValue();
    assertThat(saved.getActorId()).isEqualTo(7L);
    assertThat(saved.getActorEmail()).isEqualTo("admin@phystech.edu");
    assertThat(saved.getActionType()).isEqualTo(AdminActionType.ROLE_CHANGE);
    assertThat(saved.getTargetType()).isEqualTo(AuditTargetType.USER);
    assertThat(saved.getTargetId()).isEqualTo(42L);
    assertThat(saved.getDetails()).isEqualTo("Назначен модератор");
  }

  @Test
  void logAdminLogin_persistsAllFields() {
    auditService.logAdminLogin("admin@phystech.edu", true, "127.0.0.1", "Mozilla");

    ArgumentCaptor<AdminLoginAudit> captor = ArgumentCaptor.forClass(AdminLoginAudit.class);
    verify(loginRepo).save(captor.capture());
    AdminLoginAudit saved = captor.getValue();
    assertThat(saved.getAdminEmail()).isEqualTo("admin@phystech.edu");
    assertThat(saved.isSuccess()).isTrue();
    assertThat(saved.getIp()).isEqualTo("127.0.0.1");
    assertThat(saved.getUserAgent()).isEqualTo("Mozilla");
  }

  @Test
  void logAdminLogin_failureCase() {
    auditService.logAdminLogin("hacker@phystech.edu", false, "1.2.3.4", "curl");
    ArgumentCaptor<AdminLoginAudit> captor = ArgumentCaptor.forClass(AdminLoginAudit.class);
    verify(loginRepo).save(captor.capture());
    assertThat(captor.getValue().isSuccess()).isFalse();
  }
}
