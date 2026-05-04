package com.mipt.portal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;
  @InjectMocks private EmailService service;

  @Test
  void sendBookingCreated_sendsTwoMails() {
    service.sendBookingCreated("buyer@x.com", "seller@x.com", "Хлеб", 10L);
    verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendBookingConfirmed_sends() {
    service.sendBookingConfirmed("buyer@x.com", "Хлеб");
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendBookingCancelled_buyerVariant() {
    service.sendBookingCancelled("user@x.com", "Хлеб", true);
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    assertThat(captor.getValue().getText()).contains("Продавец отменил");
  }

  @Test
  void sendBookingCancelled_sellerVariant() {
    service.sendBookingCancelled("user@x.com", "Хлеб", false);
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    assertThat(captor.getValue().getText()).contains("Вы отменили");
  }

  @Test
  void sendWelcome_sends() {
    service.sendWelcome("u@x.com", "Ivan");
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendAdApproved_sends() {
    service.sendAdApproved("u@x.com", "Title");
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendAdRejected_includesReason() {
    service.sendAdRejected("u@x.com", "Title", "spam");
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    assertThat(captor.getValue().getText()).contains("Причина: spam");
  }

  @Test
  void sendAdRejected_blankReasonOmitted() {
    service.sendAdRejected("u@x.com", "Title", "");
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    assertThat(captor.getValue().getText()).doesNotContain("Причина:");
  }

  @Test
  void sendAdRejected_nullReasonOmitted() {
    service.sendAdRejected("u@x.com", "Title", null);
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendAdDeleted_sends() {
    service.sendAdDeleted("u@x.com", "Title", "spam");
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendAdDeleted_nullReason() {
    service.sendAdDeleted("u@x.com", "Title", null);
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendSanctionApplied_freezeVariant() {
    service.sendSanctionApplied("u@x.com", "freeze", "spam", 24);
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    assertThat(captor.getValue().getText()).contains("заморожен");
  }

  @Test
  void sendSanctionApplied_banVariant() {
    service.sendSanctionApplied("u@x.com", "ban", "spam", 48);
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    assertThat(captor.getValue().getText()).contains("заблокирован");
  }

  @Test
  void sendSanctionApplied_blankReason() {
    service.sendSanctionApplied("u@x.com", "freeze", "", 12);
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendSanctionLifted_sends() {
    service.sendSanctionLifted("u@x.com");
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail_swallowsExceptions() {
    doThrow(new RuntimeException("smtp")).when(mailSender).send(any(SimpleMailMessage.class));
    // Should not propagate
    service.sendWelcome("u@x.com", "Ivan");
  }
}
