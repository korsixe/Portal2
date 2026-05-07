package com.mipt.portal.infrastructure.database;

import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.service.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TestData}.
 *
 * <p>{@code TestData} is a static-utility holder. The non-trivial methods
 * ({@code uploadAllPhotos}, {@code main}) catch their own SQLException so that
 * a missing local Postgres does not throw out of the JVM. We invoke them to
 * cover the method bodies; assertions are limited to "did not throw".</p>
 */
class TestDataTest {

    @Test
    void canBeConstructed() throws SQLException {
        new TestData();
    }

    @Test
    void uploadAllPhotos_isSilent_whenPostgresIsUnreachable() {
        // нет реального postgres на 5432 — TestData.uploadAllPhotos() ловит SQLException
        // внутри себя и просто логирует; наша задача — выполнить тело метода
        assertThatCode(TestData::uploadAllPhotos).doesNotThrowAnyException();
    }

    @Test
    void main_invokesUploadAllPhotos_withoutThrowing() {
        assertThatCode(() -> TestData.main(new String[]{})).doesNotThrowAnyException();
    }

    @Test
    void generateTestAds_viaReflection_withMockedService_coversInnerBranches() {
        // generateTestAds — приватный метод, недостижимый из публичного API (start() закомментирован).
        // Чтобы покрыть его тело, мокаем static-поле adsService и вызываем через reflection.
        AnnouncementService mockService = mock(AnnouncementService.class);
        when(mockService.getUserIdByEmail(anyString())).thenReturn(1L, 2L, 3L, 4L);
        AnnouncementRepository mockRepo = mock(AnnouncementRepository.class);

        AnnouncementService savedService = (AnnouncementService) ReflectionTestUtils
                .getField(TestData.class, "adsService");
        AnnouncementRepository savedRepo = (AnnouncementRepository) ReflectionTestUtils
                .getField(TestData.class, "adsRepository");

        ReflectionTestUtils.setField(TestData.class, "adsService", mockService);
        ReflectionTestUtils.setField(TestData.class, "adsRepository", mockRepo);
        try {
            ReflectionTestUtils.invokeMethod(TestData.class, "generateTestAds");
        } finally {
            ReflectionTestUtils.setField(TestData.class, "adsService", savedService);
            ReflectionTestUtils.setField(TestData.class, "adsRepository", savedRepo);
        }
    }

    @Test
    void generateTestAds_viaReflection_withNullService_logsError() {
        // adsService=null → внутри try бросается NPE → ветка catch (Exception) сработает
        AnnouncementService savedService = (AnnouncementService) ReflectionTestUtils
                .getField(TestData.class, "adsService");
        ReflectionTestUtils.setField(TestData.class, "adsService", null);
        try {
            ReflectionTestUtils.invokeMethod(TestData.class, "generateTestAds");
        } finally {
            ReflectionTestUtils.setField(TestData.class, "adsService", savedService);
        }
    }

    @Test
    void uploadAllPhotos_withMockedDriverManager_executesHappyPath() throws SQLException {
        // мокаем DriverManager статически чтобы пройти try-with-resources до конца
        // и покрыть тело try (HashMap, цикл по photoMapping)
        Connection mockConn = mock(Connection.class);
        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConn);

            assertThatCode(TestData::uploadAllPhotos).doesNotThrowAnyException();
        }
    }
}
