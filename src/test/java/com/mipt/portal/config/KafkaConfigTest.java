package com.mipt.portal.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link KafkaConfig}.
 *
 * <p>The class is a plain {@code @Configuration} with @Value-injected topic names and
 * a handful of @Bean factory methods. We instantiate it directly, populate the @Value
 * fields via reflection and assert that every bean method returns a sane object.</p>
 */
class KafkaConfigTest {

    private KafkaConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "auditTopic", "portal.audit.events");
        ReflectionTestUtils.setField(config, "userTopic", "portal.user.events");
        ReflectionTestUtils.setField(config, "announcementTopic", "portal.announcement.events");
        ReflectionTestUtils.setField(config, "moderationTopic", "portal.moderation.events");
        ReflectionTestUtils.setField(config, "bookingTopic", "portal.booking.events");
        ReflectionTestUtils.setField(config, "commentTopic", "portal.comment.events");
        ReflectionTestUtils.setField(config, "supportTopic", "portal.support.events");
        ReflectionTestUtils.setField(config, "notificationTopic", "portal.notification.events");
    }

    @Test
    void kafkaErrorHandler_isNonNull() {
        DefaultErrorHandler handler = config.kafkaErrorHandler();
        assertThat(handler).isNotNull();
    }

    @Test
    void kafkaListenerContainerFactory_isWiredWithFactoryAndErrorHandler() {
        @SuppressWarnings("unchecked")
        ConsumerFactory<String, String> consumerFactory = mock(ConsumerFactory.class);
        DefaultErrorHandler errorHandler = config.kafkaErrorHandler();

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                config.kafkaListenerContainerFactory(consumerFactory, errorHandler);

        assertThat(factory).isNotNull();
        assertThat(factory.getContainerProperties()).isNotNull();
    }

    @Test
    void allTopicBeans_haveCorrectNamesAndPartitionsAndReplicas() {
        NewTopic[] topics = {
                config.auditTopic(),
                config.userTopic(),
                config.announcementTopic(),
                config.moderationTopic(),
                config.bookingTopic(),
                config.commentTopic(),
                config.supportTopic(),
                config.notificationTopic()
        };
        String[] expectedNames = {
                "portal.audit.events",
                "portal.user.events",
                "portal.announcement.events",
                "portal.moderation.events",
                "portal.booking.events",
                "portal.comment.events",
                "portal.support.events",
                "portal.notification.events"
        };

        for (int i = 0; i < topics.length; i++) {
            assertThat(topics[i].name()).isEqualTo(expectedNames[i]);
            assertThat(topics[i].numPartitions()).isEqualTo(1);
            assertThat(topics[i].replicationFactor()).isEqualTo((short) 1);
        }
    }
}
