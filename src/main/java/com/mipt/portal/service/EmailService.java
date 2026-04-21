package com.mipt.portal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendBookingCreated(String buyerEmail, String sellerEmail, String adTitle, long adId) {
        sendEmail(buyerEmail,
                "Бронирование подтверждено — " + adTitle,
                "Вы забронировали товар «" + adTitle + "». " +
                "Продавец свяжется с вами для завершения сделки. " +
                "Посмотреть объявление: http://localhost:3000/ad/" + adId);

        sendEmail(sellerEmail,
                "Ваш товар забронирован — " + adTitle,
                "Ваш товар «" + adTitle + "» был забронирован. " +
                "Свяжитесь с покупателем и подтвердите продажу через сайт. " +
                "Посмотреть объявление: http://localhost:3000/ad/" + adId);
    }

    @Async
    public void sendBookingConfirmed(String buyerEmail, String adTitle) {
        sendEmail(buyerEmail,
                "Продажа подтверждена — " + adTitle,
                "Продавец подтвердил продажу товара «" + adTitle + "». Спасибо за использование Portal!");
    }

    @Async
    public void sendBookingCancelled(String recipientEmail, String adTitle, boolean isBuyer) {
        String who = isBuyer ? "Продавец отменил" : "Вы отменили";
        sendEmail(recipientEmail,
                "Бронирование отменено — " + adTitle,
                who + " бронирование товара «" + adTitle + "». Товар снова доступен для бронирования.");
    }

    @Async
    public void sendWelcome(String email, String name) {
        sendEmail(email,
                "Добро пожаловать в Portal!",
                "Привет, " + name + "! Вы успешно зарегистрировались на Portal. " +
                "Начните с просмотра объявлений: http://localhost:3000");
    }

    @Async
    public void sendAdApproved(String email, String adTitle) {
        sendEmail(email,
                "Объявление опубликовано — " + adTitle,
                "Ваше объявление «" + adTitle + "» прошло модерацию и теперь доступно всем пользователям.");
    }

    @Async
    public void sendAdRejected(String email, String adTitle, String reason) {
        sendEmail(email,
                "Объявление отклонено — " + adTitle,
                "Ваше объявление «" + adTitle + "» было отклонено модератором." +
                (reason != null && !reason.isBlank() ? "\nПричина: " + reason : ""));
    }

    @Async
    public void sendAdDeleted(String email, String adTitle, String reason) {
        sendEmail(email,
                "Объявление удалено — " + adTitle,
                "Ваше объявление «" + adTitle + "» было удалено модератором." +
                (reason != null && !reason.isBlank() ? "\nПричина: " + reason : ""));
    }

    @Async
    public void sendSanctionApplied(String email, String type, String reason, int durationHours) {
        String action = "freeze".equalsIgnoreCase(type) ? "заморожен" : "заблокирован";
        sendEmail(email,
                "Ваш аккаунт " + action,
                "Ваш аккаунт был " + action + " на " + durationHours + " ч." +
                (reason != null && !reason.isBlank() ? "\nПричина: " + reason : "") +
                "\nПо вопросам обращайтесь в поддержку.");
    }

    @Async
    public void sendSanctionLifted(String email) {
        sendEmail(email,
                "Ограничения сняты",
                "Ограничения с вашего аккаунта на Portal были сняты. Добро пожаловать обратно!");
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
