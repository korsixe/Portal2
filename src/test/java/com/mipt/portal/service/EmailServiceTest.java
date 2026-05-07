package com.mipt.portal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link EmailService}.
 *
 * <p>All methods are thin shells that build a SimpleMailMessage and delegate to
 * JavaMailSender. We mock the sender, capture the produced messages and assert that
 * subject and body contain the expected user-facing pieces. The catch branch in
 * {@code sendEmail()} is exercised separately by making the sender throw.</p>
 */
class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService service;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        service = new EmailService(mailSender);
    }

    private SimpleMailMessage capture() {
        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(cap.capture());
        return cap.getValue();
    }

    @Test
    void sendBookingCreated_sendsTwoMessages_withAdLinkAndTitle() {
        service.sendBookingCreated("buyer@x", "seller@x", "iPhone", 42L);

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(cap.capture());

        SimpleMailMessage toBuyer = cap.getAllValues().get(0);
        SimpleMailMessage toSeller = cap.getAllValues().get(1);

        assertThat(toBuyer.getTo()).containsExactly("buyer@x");
        assertThat(toBuyer.getSubject()).contains("iPhone");
        assertThat(toBuyer.getText()).contains("http://localhost:3000/ad/42");

        assertThat(toSeller.getTo()).containsExactly("seller@x");
        assertThat(toSeller.getText()).contains("http://localhost:3000/ad/42");
    }

    @Test
    void sendBookingConfirmed_sendsConfirmationToBuyer() {
        service.sendBookingConfirmed("buyer@x", "iPhone");

        SimpleMailMessage msg = capture();
        assertThat(msg.getTo()).containsExactly("buyer@x");
        assertThat(msg.getSubject()).contains("Продажа подтверждена");
        assertThat(msg.getText()).contains("iPhone");
    }

    @Test
    void sendBookingCancelled_buyerVersion() {
        service.sendBookingCancelled("buyer@x", "iPhone", true);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).contains("Продавец отменил");
    }

    @Test
    void sendBookingCancelled_sellerVersion() {
        service.sendBookingCancelled("seller@x", "iPhone", false);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).contains("Вы отменили");
    }

    @Test
    void sendWelcome_includesUserNameAndHomeLink() {
        service.sendWelcome("user@x", "Иван");

        SimpleMailMessage msg = capture();
        assertThat(msg.getSubject()).contains("Добро пожаловать");
        assertThat(msg.getText()).contains("Иван").contains("http://localhost:3000");
    }

    @Test
    void sendAdApproved_subjectMentionsAdTitle() {
        service.sendAdApproved("u@x", "Объявление-1");

        SimpleMailMessage msg = capture();
        assertThat(msg.getSubject()).contains("Объявление-1");
    }

    @Test
    void sendAdRejected_withReason_includesReason() {
        service.sendAdRejected("u@x", "Лампа", "слишком дорого");

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).contains("слишком дорого");
    }

    @Test
    void sendAdRejected_withoutReason_doesNotIncludeReasonLine() {
        service.sendAdRejected("u@x", "Лампа", null);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).doesNotContain("Причина:");
    }

    @Test
    void sendAdRejected_withBlankReason_doesNotIncludeReasonLine() {
        service.sendAdRejected("u@x", "Лампа", "   ");

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).doesNotContain("Причина:");
    }

    @Test
    void sendAdDeleted_withReason_includesReason() {
        service.sendAdDeleted("u@x", "Лампа", "нарушение");

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).contains("нарушение");
    }

    @Test
    void sendAdDeleted_withoutReason_omitsReasonLine() {
        service.sendAdDeleted("u@x", "Лампа", null);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).doesNotContain("Причина:");
    }

    @Test
    void sendSanctionApplied_freezeWithReason() {
        service.sendSanctionApplied("u@x", "freeze", "spam", 24);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).contains("заморожен").contains("24 ч").contains("spam");
    }

    @Test
    void sendSanctionApplied_banWithoutReason() {
        service.sendSanctionApplied("u@x", "ban", null, 48);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).contains("заблокирован").contains("48 ч").doesNotContain("Причина:");
    }

    @Test
    void sendSanctionApplied_banWithBlankReason() {
        service.sendSanctionApplied("u@x", "ban", "   ", 48);

        SimpleMailMessage msg = capture();
        assertThat(msg.getText()).doesNotContain("Причина:");
    }

    @Test
    void sendSanctionLifted_isInformative() {
        service.sendSanctionLifted("u@x");

        SimpleMailMessage msg = capture();
        assertThat(msg.getSubject()).contains("Ограничения сняты");
    }

    @Test
    void sendEmail_swallowsExceptionsFromMailSender() {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        // Любой публичный метод дойдёт до sendEmail; убедимся что исключение проглочено и логируется
        assertThatCode(() -> service.sendWelcome("u@x", "Иван")).doesNotThrowAnyException();
    }
}
