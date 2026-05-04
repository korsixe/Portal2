package com.mipt.portal.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mipt.portal.PortalApplication;
import com.mipt.portal.controller.LegacyAdminModeratorRedirectController;
import com.mipt.portal.controller.ReactController;
import com.mipt.portal.service.KafkaEventConsumer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.mockito.Mockito.mock;

/**
 * Slim Spring Boot context for controller integration tests:
 *  - JPA repositories enabled, H2 used via {@code application-test.yml},
 *  - Kafka and Elasticsearch repository auto-configurations disabled,
 *  - {@link KafkaTemplate} and {@link ElasticsearchOperations} replaced with no-op mocks
 *    so beans that depend on them ({@code KafkaMessageService}, {@code ElasticSearchService})
 *    can still wire up,
 *  - {@code DataInitializer} (CommandLineRunner) and Kafka consumers excluded so tests
 *    don't seed data or attach listeners.
 */
@Configuration
@EnableAsync
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.mipt.portal.entity")
@EnableJpaRepositories(basePackages = "com.mipt.portal")
@EnableAutoConfiguration(exclude = {
    KafkaAutoConfiguration.class,
    ElasticsearchRepositoriesAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.mipt.portal",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
            PortalApplication.class,
            com.mipt.portal.config.DataInitializer.class,
            com.mipt.portal.config.KafkaConfig.class,
            KafkaEventConsumer.class,
            ReactController.class,
            LegacyAdminModeratorRedirectController.class
        })
    }
)
public class AbstractWebMvcTest {

    @Bean
    @Primary
    @SuppressWarnings({"unchecked", "rawtypes"})
    public KafkaTemplate<String, String> kafkaTemplate() {
        return (KafkaTemplate) mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchOperations() {
        return mock(ElasticsearchOperations.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
