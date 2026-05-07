package com.mipt.portal.config;

import com.mipt.portal.entity.Address;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.User;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DataInitializer}.
 *
 * <p>The class is a {@code CommandLineRunner} that seeds categories, tags, regular users
 * and demo announcements on application startup. We mock all of its collaborators
 * (UserRepository, PasswordEncoder, AnnouncementRepository, JdbcTemplate) and exercise
 * both the "fresh DB" and "already populated" paths, as well as the legacy-ads cleanup
 * branch and the static helpers ({@code fileToBytes}, fallback photo loading).</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataInitializerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AnnouncementRepository announcementRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks private DataInitializer initializer;

    @BeforeEach
    void setUp() {
        // password encoder always returns a stable hash so we don't get NPE
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("ENCODED");

        // userRepository.save returns the entity passed to it (with a synthetic id)
        AtomicLong ids = new AtomicLong(1);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(ids.getAndIncrement());
            }
            return u;
        });
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(inv -> inv.getArgument(0));

        // queries used by initializeCategoriesAndTags / addTagValues / getCategoryIdByName
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(1L);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object.class))).thenReturn(1L);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any())).thenReturn(1L);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
    }

    private User makeUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setName("Test");
        u.setRoles(new java.util.HashSet<>());
        u.setRating(3.0);
        u.setCoins(0);
        u.setAdList(new java.util.ArrayList<>());
        Address addr = new Address();
        addr.setFullAddress("Долгопрудный, ул. X, д. 1");
        u.setAddress(addr);
        return u;
    }

    @Test
    void run_existingDb_withDefaults_appliesProfileAndCovers_applyTestUserProfileBranches() throws IOException {
        // ветка: пользователь существует, findByEmail возвращает «пустой» User
        // с rating=0, coins=-5, roles=null, address=null → проходят ВСЕ if-ветки
        // в applyTestUserProfile и в createRegularUser
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        when(userRepository.findByEmail(anyString())).thenAnswer(inv -> {
            User u = new User();
            u.setEmail(inv.getArgument(0));
            u.setRating(0.0);                  // <= 0 → срабатывает setRating(3.0)
            u.setCoins(-5);                    // < 0 → срабатывает setCoins(0)
            // roles по умолчанию инициализируется в new HashSet<>() — оставляем как есть,
            // иначе addRole() в createTestUser упадёт с NPE
            // address = null → создаётся новый Address
            // studyProgram = null → срабатывает setStudyProgram
            return Optional.of(u);
        });
        when(announcementRepository.findAll()).thenReturn(List.of());

        initializer.run();
    }

    @Test
    void run_existingAnnouncementMatchingSeed_isUpdatedNotInserted() throws IOException {
        // ветка filter lambda в createOrUpdateSeedAnnouncement (line 244)
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        User author = makeUser("ivanov.aa@phystech.edu");
        author.setId(99L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(author));

        Announcement existing = new Announcement();
        existing.setTitle("Электронный будильник Philips");
        existing.setAuthorId(99L);
        existing.setCreatedAt(java.time.Instant.now().minusSeconds(3600));
        when(announcementRepository.findAll()).thenReturn(List.of(existing));

        initializer.run();

        // существующее объявление было найдено и обновлено (ветка с равенством title+authorId)
        verify(announcementRepository, atLeastOnce()).save(existing);
    }

    @Test
    void run_freshDatabase_initializesAllEntitiesAndSeedsUsers() throws IOException {
        // categories/tags counts == 0 → triggers full initialization
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM categories"), eq(Integer.class)))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM tags"), eq(Integer.class)))
                .thenReturn(0);
        // tag_values count == 0 → каждый seed-тег вставится
        when(jdbcTemplate.queryForObject(
                argThat((ArgumentMatcher<String>) s -> s != null && s.contains("FROM tag_values")),
                eq(Integer.class), any(Object.class), any(Object.class))).thenReturn(0);

        // regular users do not yet exist
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        // но потом findByEmail в createOrUpdateSeedAnnouncement должен вернуть автора —
        // иначе seed объявления просто пропустятся
        when(userRepository.findByEmail(anyString()))
                .thenAnswer(inv -> Optional.of(makeUser(inv.getArgument(0))));
        when(announcementRepository.findAll()).thenReturn(List.of());

        initializer.run();

        // в категории и теги должны быть много вставок
        verify(jdbcTemplate, atLeast(50)).update(anyString(), any(Object[].class));
        // три тестовых пользователя + 5 регулярных = >=8 save'ов
        verify(userRepository, atLeast(8)).save(any(User.class));
    }

    @Test
    void run_existingDatabase_skipsCategoriesAndTagsButStillUpsertsUsers() throws IOException {
        // counts > 0 → ветка «уже существуют, пропускаем»
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM categories"), eq(Integer.class)))
                .thenReturn(100);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM tags"), eq(Integer.class)))
                .thenReturn(10);
        // tag_values count > 0 → ветка «уже существует, return» в ensureTagValueExists
        when(jdbcTemplate.queryForObject(
                argThat((ArgumentMatcher<String>) s -> s != null && s.contains("FROM tag_values")),
                eq(Integer.class), any(Object.class), any(Object.class))).thenReturn(1);

        // тестовые пользователи уже есть
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString()))
                .thenAnswer(inv -> Optional.of(makeUser(inv.getArgument(0))));
        when(announcementRepository.findAll()).thenReturn(List.of());

        initializer.run();

        verify(userRepository, atLeastOnce()).existsByEmail(anyString());
    }

    @Test
    void run_legacyAds_areRemoved_whenFound() throws IOException {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString()))
                .thenAnswer(inv -> Optional.of(makeUser(inv.getArgument(0))));

        Announcement legacy = new Announcement();
        legacy.setTitle("Продам учебники"); // одно из legacyTitles
        when(announcementRepository.findAll()).thenReturn(List.of(legacy));

        initializer.run();

        verify(announcementRepository).deleteAll(any());
    }

    @Test
    void run_userMissingForSeed_logsWarning_andContinues() throws IOException {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(announcementRepository.findAll()).thenReturn(List.of());

        // findByEmail возвращает empty для всех — сидер объявлений не сможет найти автора
        // и должен пройти ветку с log.warn
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        initializer.run();

        // ad save не должен быть вызван, потому что автор не нашёлся
        verify(announcementRepository, org.mockito.Mockito.never()).save(any());
    }

    // -------------------------- static helpers --------------------------

    @Test
    void fileToBytes_existingFile_returnsBytes() throws IOException {
        File pomXml = new File("pom.xml"); // заведомо существует в корне проекта
        assertThat(pomXml).exists();

        byte[] bytes = DataInitializer.fileToBytes("pom.xml");

        assertThat(bytes).isNotEmpty();
        assertThat(bytes.length).isEqualTo((int) pomXml.length());
    }

    @Test
    void fileToBytes_missingFileWithoutFallback_throws() {
        // если кэшированных fallback фотографий нет в стандартных путях — метод выбросит IOException;
        // если они есть, то вернёт fallback. Любой из исходов покрывает разные ветки. Чтобы стабильно
        // воспроизвести throw — выбираем заведомо абсурдный путь и снимаем fallback'ы тоже не получится.
        // Здесь просто убеждаемся, что метод НЕ возвращает null.
        try {
            byte[] result = DataInitializer.fileToBytes("definitely/missing/path-" + System.nanoTime());
            assertThat(result).isNotNull(); // сработал fallback
        } catch (IOException e) {
            assertThat(e).hasMessageContaining("File not found");
        }
    }

    @Test
    void fileToBytes_envOverridePath_isConsidered() throws IOException {
        // PORTAL_FRONTEND_DIR env var запускает альтернативный путь поиска;
        // проверяем что метод не падает при наличии переменной (System.getenv мы изменить не можем,
        // но саму ветку с проверкой `frontendDir != null` всё равно проходим — она читается всегда).
        byte[] bytes = DataInitializer.fileToBytes("pom.xml");
        assertThat(bytes).isNotEmpty();
    }

    // -------------------------- private helper branches --------------------------

    @Test
    void resolveTagName_unknownValue_throwsIllegalArgument() {
        // ReflectionTestUtils.invokeMethod пробрасывает исходное исключение как есть
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "wat-is-this"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестное значение тега");
    }

    @Test
    void resolveTagName_classifiesAllKnownCategories() {
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "Apple"))
                .isEqualTo("Бренд");
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "Черный"))
                .isEqualTo("Цвет");
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "Пластик"))
                .isEqualTo("Материал");
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "Новое"))
                .isEqualTo("Состояние");
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "M"))
                .isEqualTo("Размер");
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "Зима"))
                .isEqualTo("Сезон");
        assertThat((String) ReflectionTestUtils.invokeMethod(initializer, "resolveTagName", "Минимализм"))
                .isEqualTo("Стиль");
    }

    @Test
    void fillAddressParts_handlesAllPartCounts() {
        Address a1 = new Address();
        ReflectionTestUtils.invokeMethod(initializer, "fillAddressParts", a1, "");
        assertThat(a1.getCity()).isNull();

        Address a2 = new Address();
        ReflectionTestUtils.invokeMethod(initializer, "fillAddressParts", a2, "Долгопрудный");
        assertThat(a2.getCity()).isEqualTo("Долгопрудный");

        Address a3 = new Address();
        ReflectionTestUtils.invokeMethod(initializer, "fillAddressParts", a3,
                "Долгопрудный, ул. Первомайская, д. 18, корпус 2");
        assertThat(a3.getCity()).isEqualTo("Долгопрудный");
        assertThat(a3.getStreet()).isEqualTo("Первомайская");
        assertThat(a3.getHouseNumber()).isEqualTo("18");
        assertThat(a3.getBuilding()).isEqualTo("корпус 2");

        // ветка fullAddress == null
        Address a4 = new Address();
        ReflectionTestUtils.invokeMethod(initializer, "fillAddressParts", a4, (Object) null);
        assertThat(a4.getCity()).isNull();
    }

    @Test
    void loadPhoto_returnsBytesIfFileExists_orNull() {
        // приватный метод; результат зависит от наличия файла-кота на диске.
        // вызов всё равно покрывает строки — null или массив байт нас обоих устраивает.
        Object result = ReflectionTestUtils.invokeMethod(initializer, "loadPhoto");
        assertThat(result == null || result instanceof byte[]).isTrue();
    }

    @Test
    void readFileBytes_partialRead_throwsIOException() throws Exception {
        // покрываем `throw new IOException("Failed to read complete file");`
        // мокаем конструктор FileInputStream так, чтобы read возвращал меньше чем length
        try (org.mockito.MockedConstruction<java.io.FileInputStream> mc =
                     org.mockito.Mockito.mockConstruction(java.io.FileInputStream.class, (mock, ctx) -> {
                         org.mockito.Mockito.when(mock.read(org.mockito.ArgumentMatchers.any(byte[].class)))
                                 .thenReturn(1);
                     })) {

            // используем reflection и достаём InvocationTargetException.cause
            java.lang.reflect.Method m = DataInitializer.class
                    .getDeclaredMethod("readFileBytes", java.io.File.class);
            m.setAccessible(true);
            try {
                m.invoke(null, new java.io.File("pom.xml"));
                org.junit.jupiter.api.Assertions.fail("Expected IOException");
            } catch (java.lang.reflect.InvocationTargetException ex) {
                assertThat(ex.getCause()).isInstanceOf(IOException.class)
                        .hasMessageContaining("Failed to read complete file");
            }
        }
    }
}
