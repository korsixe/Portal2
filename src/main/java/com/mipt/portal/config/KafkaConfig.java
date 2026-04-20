package com.mipt.portal.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${app.kafka.topic.audit:portal.audit.events}")
  private String auditTopic;

  @Value("${app.kafka.topic.user:portal.user.events}")
  private String userTopic;

  @Value("${app.kafka.topic.announcement:portal.announcement.events}")
  private String announcementTopic;

  @Value("${app.kafka.topic.moderation:portal.moderation.events}")
  private String moderationTopic;

  @Value("${app.kafka.topic.booking:portal.booking.events}")
  private String bookingTopic;

  @Value("${app.kafka.topic.comment:portal.comment.events}")
  private String commentTopic;

  @Value("${app.kafka.topic.support:portal.support.events}")
  private String supportTopic;

  @Value("${app.kafka.topic.notification:portal.notification.events}")
  private String notificationTopic;

  @Bean
  public NewTopic auditTopic() {
    return new NewTopic(auditTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic userTopic() {
    return new NewTopic(userTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic announcementTopic() {
    return new NewTopic(announcementTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic moderationTopic() {
    return new NewTopic(moderationTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic bookingTopic() {
    return new NewTopic(bookingTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic commentTopic() {
    return new NewTopic(commentTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic supportTopic() {
    return new NewTopic(supportTopic, 1, (short) 1);
  }

  @Bean
  public NewTopic notificationTopic() {
    return new NewTopic(notificationTopic, 1, (short) 1);
  }
}
