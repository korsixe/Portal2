package com.mipt.portal.config;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

class MailConfigTest {

    @Test
    void javaMailSender_returnsJavaMailSenderImpl() {
        JavaMailSender sender = new MailConfig().javaMailSender();

        assertThat(sender).isInstanceOf(JavaMailSenderImpl.class);
    }
}
