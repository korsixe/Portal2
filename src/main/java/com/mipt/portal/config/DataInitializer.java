package com.mipt.portal.config;

import com.mipt.portal.enums.Role;
import com.mipt.portal.entity.User;
import com.mipt.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.mipt.portal.entity.Address;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import com.mipt.portal.repository.AnnouncementRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnnouncementRepository announcementRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        initializeCategoriesAndTags();

        User admin = createTestUser("admin.test@phystech.edu", "admin", Role.ADMIN);
        User moderator = createTestUser("moderator.test@phystech.edu", "moderator", Role.MODERATOR);
        createTestUser("user.test@phystech.edu", "user", Role.USER);
        ensureAdditionalRegularUsers();
        removeLegacyPixelSeedAnnouncements();
        ensureAdditionalRegularUserAnnouncements();
    }

    private void ensureAdditionalRegularUsers() {
        List<RegularUserSeed> seeds = List.of(
            new RegularUserSeed("ivanov.aa@phystech.edu", "Алексей Иванов", "student1", "ФПМИ", 1, "Долгопрудный, ул. Первомайская, д. 18"),
            new RegularUserSeed("petrova.mv@phystech.edu", "Мария Петрова", "student2", "ФРКТ", 2, "Москва, ул. Бауманская, д. 7"),
            new RegularUserSeed("sidorov.dk@phystech.edu", "Даниил Сидоров", "student3", "ЛФИ", 3, "Долгопрудный, Лихачевский проспект, д. 64"),
            new RegularUserSeed("smirnova.ea@phystech.edu", "Елена Смирнова", "student4", "ФЭФМ", 4, "Москва, Дмитровское шоссе, д. 107"),
            new RegularUserSeed("kozlov.np@phystech.edu", "Никита Козлов", "student5", "ВШПИ", 5, "Долгопрудный, ул. Спортивная, д. 9")
        );

        for (RegularUserSeed seed : seeds) {
            createRegularUser(seed);
        }
    }

    private void ensureAdditionalRegularUserAnnouncements() throws IOException {
        List<TestAdSeed> seeds = List.of(
            new TestAdSeed("Алексей Иванов", "ivanov.aa@phystech.edu", "Электронный будильник Philips", "Компактный будильник с ярким дисплеем, работает без сбоев.", Category.HOME, "Освещение", Condition.USED, 1200, "Долгопрудный, ул. Первомайская, д. 18", "src/main/frontend/public/images/test_ads/alarm_clock.jpeg", List.of("Philips", "Черный", "Пластик", "Б/у хорошее")),
            new TestAdSeed("Алексей Иванов", "ivanov.aa@phystech.edu", "Bluetooth-колонка Sony", "Портативная колонка с чистым звуком и хорошим аккумулятором.", Category.ELECTRONICS, "Аксессуары", Condition.USED, 3500, "Долгопрудный, ул. Первомайская, д. 18", "src/main/frontend/public/images/test_ads/bluetooth_speaker.jpeg", List.of("Sony", "Синий", "Пластик", "Б/у отличное")),
            new TestAdSeed("Алексей Иванов", "ivanov.aa@phystech.edu", "Ковер для гостиной", "Мягкий домашний ковер, чистый и без повреждений.", Category.HOME, "Текстиль", Condition.USED, 4200, "Долгопрудный, ул. Первомайская, д. 18", "src/main/frontend/public/images/test_ads/carpet.jpeg", List.of("Бежевый", "Шерсть", "Б/у хорошее", "Современный")),

            new TestAdSeed("Мария Петрова", "petrova.mv@phystech.edu", "Компьютерная мышь Microsoft", "Удобная беспроводная мышь для учебы и работы.", Category.ELECTRONICS, "Компьютеры", Condition.USED, 1800, "Москва, ул. Бауманская, д. 7", "src/main/frontend/public/images/test_ads/computer_mouse.jpeg", List.of("Microsoft", "Серый", "Пластик", "Б/у отличное")),
            new TestAdSeed("Мария Петрова", "petrova.mv@phystech.edu", "Белый комод для комнаты", "Вместительный комод в хорошем состоянии, отлично подойдет в спальню.", Category.HOME, "Мебель", Condition.USED, 7800, "Москва, ул. Бауманская, д. 7", "src/main/frontend/public/images/test_ads/dresser.jpeg", List.of("Белый", "Дерево", "Б/у хорошее", "Минимализм")),
            new TestAdSeed("Мария Петрова", "petrova.mv@phystech.edu", "Проводные наушники Sony", "Легкие наушники для ежедневного использования, звучат чисто.", Category.ELECTRONICS, "Наушники", Condition.USED, 2300, "Москва, ул. Бауманская, д. 7", "src/main/frontend/public/images/test_ads/earphones.jpeg", List.of("Sony", "Черный", "Пластик", "Б/у хорошее")),

            new TestAdSeed("Даниил Сидоров", "sidorov.dk@phystech.edu", "Футбольный мяч", "Практически новый мяч для игры на улице и в зале.", Category.SPORTS, "Игровые виды", Condition.NEW, 1600, "Долгопрудный, Лихачевский проспект, д. 64", "src/main/frontend/public/images/test_ads/football.jpeg", List.of("Белый", "Резина", "Новое", "Спортивный")),
            new TestAdSeed("Даниил Сидоров", "sidorov.dk@phystech.edu", "iPhone в хорошем состоянии", "Рабочий смартфон Apple, аккумулятор держит уверенно.", Category.ELECTRONICS, "Смартфоны", Condition.USED, 29900, "Долгопрудный, Лихачевский проспект, д. 64", "src/main/frontend/public/images/test_ads/iphone.jpg", List.of("Apple", "Черный", "Стекло", "Б/у отличное")),
            new TestAdSeed("Даниил Сидоров", "sidorov.dk@phystech.edu", "Клавиатура для ПК", "Полноразмерная клавиатура для компьютера, клавиши работают мягко.", Category.ELECTRONICS, "Компьютеры", Condition.USED, 2500, "Долгопрудный, Лихачевский проспект, д. 64", "src/main/frontend/public/images/test_ads/keyboard.jpeg", List.of("Microsoft", "Черный", "Пластик", "Б/у хорошее")),

            new TestAdSeed("Елена Смирнова", "smirnova.ea@phystech.edu", "Настольная лампа Philips", "Яркая лампа для рабочего стола, отлично подходит для учебы.", Category.HOME, "Освещение", Condition.USED, 2100, "Москва, Дмитровское шоссе, д. 107", "src/main/frontend/public/images/test_ads/lamp.jpeg", List.of("Philips", "Белый", "Металл", "Б/у отличное")),
            new TestAdSeed("Елена Смирнова", "smirnova.ea@phystech.edu", "Ноутбук Lenovo для учебы", "Ноутбук в рабочем состоянии, браузер и офис тянет без проблем.", Category.ELECTRONICS, "Ноутбуки", Condition.USED, 25500, "Москва, Дмитровское шоссе, д. 107", "src/main/frontend/public/images/test_ads/laptop.jpg", List.of("Lenovo", "Серый", "Металл", "Б/у хорошее")),
            new TestAdSeed("Елена Смирнова", "smirnova.ea@phystech.edu", "Микроволновка LG", "Компактная микроволновая печь для общежития или съемной квартиры.", Category.HOME, "Бытовая техника", Condition.USED, 4900, "Москва, Дмитровское шоссе, д. 107", "src/main/frontend/public/images/test_ads/microwave.jpg", List.of("LG", "Белый", "Металл", "Б/у хорошее")),

            new TestAdSeed("Никита Козлов", "kozlov.np@phystech.edu", "Набор тарелок", "Керамические тарелки без сколов, использовались аккуратно.", Category.HOME, "Посуда", Condition.USED, 1700, "Долгопрудный, ул. Спортивная, д. 9", "src/main/frontend/public/images/test_ads/plate.jpg", List.of("Белый", "Керамика", "Б/у отличное", "Классический")),
            new TestAdSeed("Никита Козлов", "kozlov.np@phystech.edu", "Теплый свитер", "Шерстяной свитер на зиму, размер M, приятный к телу.", Category.CLOTHING, "Мужская одежда", Condition.USED, 2200, "Долгопрудный, ул. Спортивная, д. 9", "src/main/frontend/public/images/test_ads/sweater.jpeg", List.of("Серый", "Шерсть", "M", "Зима")),
            new TestAdSeed("Никита Козлов", "kozlov.np@phystech.edu", "Электрический чайник Bosch", "Быстро кипятит воду, корпус без трещин и запаха.", Category.HOME, "Бытовая техника", Condition.USED, 2600, "Долгопрудный, ул. Спортивная, д. 9", "src/main/frontend/public/images/test_ads/teapot.jpg", List.of("Bosch", "Серебристый", "Металл", "Б/у хорошее"))
        );

        ensureTagValuesForSeeds(seeds);

        for (TestAdSeed seed : seeds) {
            createOrUpdateSeedAnnouncement(seed);
        }
    }

    private void initializeCategoriesAndTags() {
        Integer categoriesCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
        Integer tagsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tags", Integer.class);

        if (categoriesCount != null && categoriesCount == 0) {
            log.info("Начинаем заполнение категорий...");
            initializeCategories();
            log.info("Категории успешно заполнены!");
        } else {
            log.info("Категории уже существуют, пропускаем инициализацию");
        }

        if (tagsCount != null && tagsCount == 0) {
            log.info("Начинаем заполнение тегов...");
            initializeTags();
            log.info("Теги успешно заполнены!");
        } else {
            log.info("Теги уже существуют, пропускаем инициализацию");
        }
    }

    private User createTestUser(String email, String password, Role role) {
        User user;
        if (!userRepository.existsByEmail(email)) {
            log.info("Creating user: {} with role: {}", email, role);
            user = new User();
            user.setEmail(email);
            String salt = UUID.randomUUID().toString().substring(0, 10);
            user.setSalt(salt);
            user.setHashPassword(passwordEncoder.encode(password + salt));
        } else {
            user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return null;
            }
        }
        String salt = UUID.randomUUID().toString().substring(0, 10);
        user.setSalt(salt);
        user.setHashPassword(passwordEncoder.encode(password + salt));
        // Гарантируем, что роль всегда есть у пользователя
        if (user.getRoles() == null || !user.getRoles().contains(role)) {
            user.addRole(role);
        }
        applyTestUserProfile(user, role);
        return userRepository.save(user);
    }

    private User createRegularUser(RegularUserSeed seed) {
        User user = userRepository.findByEmail(seed.email()).orElseGet(User::new);

        user.setEmail(seed.email());
        String salt = UUID.randomUUID().toString().substring(0, 10);
        user.setSalt(salt);
        user.setHashPassword(passwordEncoder.encode(seed.password() + salt));
        user.setName(seed.name());
        user.setStudyProgram(seed.studyProgram());
        user.setCourse(seed.course());

        if (user.getAdList() == null) {
            user.setAdList(new ArrayList<>());
        }

        if (user.getRoles() == null) {
            user.setRoles(new java.util.HashSet<>());
        }
        user.getRoles().clear();
        user.addRole(Role.USER);

        Address address = user.getAddress() != null ? user.getAddress() : new Address();
        address.setFullAddress(seed.fullAddress());
        fillAddressParts(address, seed.fullAddress());
        user.setAddress(address);

        if (user.getRating() <= 0) {
            user.setRating(3.0);
        }
        if (user.getCoins() < 0) {
            user.setCoins(0);
        }

        log.info("Ensured regular test user {}", seed.email());
        return userRepository.save(user);
    }

    private void ensureTagValuesForSeeds(List<TestAdSeed> seeds) {
        for (TestAdSeed seed : seeds) {
            for (String tagValue : seed.tags()) {
                ensureTagValueExists(resolveTagName(tagValue), tagValue);
            }
        }
    }

    private void ensureTagValueExists(String tagName, String value) {
        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM tag_values tv
            JOIN tags t ON t.id = tv.tag_id
            WHERE t.name = ? AND tv.value = ?
            """,
            Integer.class,
            tagName,
            value
        );

        if (count != null && count > 0) {
            return;
        }

        Long tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, tagName);
        jdbcTemplate.update("INSERT INTO tag_values (tag_id, value) VALUES (?, ?)", tagId, value);
        log.info("Добавлено значение тега '{}' -> '{}'", tagName, value);
    }

    private String resolveTagName(String value) {
        if (List.of("Apple", "Samsung", "Xiaomi", "Sony", "LG", "Nike", "Adidas", "Zara", "H&M", "Bosch", "Philips", "Canon", "Nikon", "Lenovo", "HP", "Dell", "Asus", "Microsoft", "Intel", "AMD").contains(value)) {
            return "Бренд";
        }
        if (List.of("Черный", "Белый", "Серый", "Красный", "Синий", "Зеленый", "Желтый", "Оранжевый", "Фиолетовый", "Розовый", "Коричневый", "Бежевый", "Голубой", "Серебристый", "Золотой", "Хаки", "Бордовый", "Бирюзовый", "Сиреневый", "Мятный").contains(value)) {
            return "Цвет";
        }
        if (List.of("Пластик", "Металл", "Стекло", "Дерево", "Керамика", "Хлопок", "Шерсть", "Лен", "Шелк", "Кожа", "Замша", "Джинса", "Флис", "Нейлон", "Полиэстер", "Резина", "Бумага", "Картон", "Акрил", "Велюр").contains(value)) {
            return "Материал";
        }
        if (List.of("Новое", "Б/у отличное", "Б/у хорошее", "Б/у удовлетворительное", "Требует ремонта", "На запчасти", "Как новое", "Следы использования").contains(value)) {
            return "Состояние";
        }
        if (List.of("XS", "S", "M", "L", "XL", "XXL", "3XL", "4XL").contains(value)) {
            return "Размер";
        }
        if (List.of("Весна", "Лето", "Осень", "Зима", "Демисезон", "Круглогодичный", "Всесезонный", "Пляжный").contains(value)) {
            return "Сезон";
        }
        if (List.of("Классический", "Современный", "Минимализм", "Хай-тек", "Винтаж", "Ретро", "Лофт", "Спортивный", "Деловой", "Повседневный").contains(value)) {
            return "Стиль";
        }
        throw new IllegalArgumentException("Неизвестное значение тега: " + value);
    }

    private void createOrUpdateSeedAnnouncement(TestAdSeed seed) throws IOException {
        User author = userRepository.findByEmail(seed.authorEmail()).orElse(null);
        if (author == null) {
            log.warn("Не найден автор {} для объявления '{}'", seed.authorEmail(), seed.title());
            return;
        }

        Announcement announcement = announcementRepository.findAll().stream()
            .filter(existing -> seed.title().equals(existing.getTitle()) && author.getId().equals(existing.getAuthorId()))
            .findFirst()
            .orElseGet(Announcement::new);

        announcement.setTitle(seed.title());
        announcement.setDescription(seed.description());
        announcement.setCategory(seed.category());
        announcement.setSubcategory(seed.subcategory());
        announcement.setCondition(seed.condition());
        announcement.setPrice(seed.price());
        announcement.setLocation(seed.location());
        announcement.setAuthorId(author.getId());
        announcement.setStatus(AdStatus.ACTIVE);
        announcement.setTags(seed.tags());
        announcement.setTagsCount(seed.tags().size());
        announcement.setPhoto(fileToBytes(seed.imagePath()));
        if (announcement.getCreatedAt() == null) {
            announcement.setCreatedAt(Instant.now());
        }
        announcement.setUpdatedAt(Instant.now());

        announcementRepository.save(announcement);
        log.info("Ensured seeded ad '{}' for {}", seed.title(), seed.authorEmail());
    }

    private void fillAddressParts(Address address, String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            return;
        }

        String[] parts = Arrays.stream(fullAddress.split(","))
            .map(String::trim)
            .filter(part -> !part.isBlank())
            .toArray(String[]::new);

        if (parts.length > 0) {
            address.setCity(parts[0]);
        }
        if (parts.length > 1) {
            address.setStreet(parts[1].replaceFirst("^ул\\.\\s*", ""));
        }
        if (parts.length > 2) {
            address.setHouseNumber(parts[2].replaceFirst("^д\\.\\s*", ""));
        }
        if (parts.length > 3) {
            address.setBuilding(parts[3]);
        }
    }

    private void applyTestUserProfile(User user, Role role) {
        user.setName("Test " + role.name());
        user.addRole(role);

        if (user.getRating() <= 0) {
            user.setRating(3.0);
        }
        if (user.getCoins() < 0) {
            user.setCoins(0);
        }
        if (user.getAdList() == null) {
            user.setAdList(new ArrayList<>());
        }

        Address address = user.getAddress() != null ? user.getAddress() : new Address();
        if (address.getCity() == null || address.getCity().isBlank()) {
            address.setCity("Москва");
        }
        if (address.getStreet() == null || address.getStreet().isBlank()) {
            address.setStreet(role == Role.USER ? "ул. Студенческая" : "ул. Академическая");
        }
        if (address.getFullAddress() == null || address.getFullAddress().isBlank()) {
            address.setFullAddress(address.getCity() + ", " + address.getStreet());
        }
        user.setAddress(address);

        if (user.getStudyProgram() == null || user.getStudyProgram().isBlank()) {
            user.setStudyProgram("Прикладная математика и информатика");
        }
        if (user.getCourse() <= 0) {
            user.setCourse(3);
        }
    }

  private void removeLegacyPixelSeedAnnouncements() {
    List<String> legacyTitles = List.of(
        "MacBook Pro 14\" 2023",
        "Смартфон Pixel 7a",
        "Продам учебники",
        "iPhone 12 128GB, аккумулятор 89%",
        "Наушники Sony WH-1000XM4, полный комплект"
    );

    List<Announcement> legacyAds = announcementRepository.findAll().stream()
        .filter(ad -> legacyTitles.contains(ad.getTitle()))
        .toList();

    if (legacyAds.isEmpty()) {
      return;
    }

    announcementRepository.deleteAll(legacyAds);
    log.info("Удалено {} старых сидовых объявлений с пиксельными фото", legacyAds.size());
  }

  private void initializeCategories() {
    List<String[]> rootCategories = Arrays.asList(
      new String[]{"Электроника", "false"},
      new String[]{"Одежда и обувь", "false"},
      new String[]{"Дом и сад", "false"},
      new String[]{"Красота и здоровье", "false"},
      new String[]{"Спорт и отдых", "false"},
      new String[]{"Детские товары", "false"},
      new String[]{"Автотовары", "false"},
      new String[]{"Книги и канцелярия", "false"},
      new String[]{"Хобби и творчество", "false"},
      new String[]{"Животные", "false"},
      new String[]{"Репетиторство", "true"},
      new String[]{"Образовательные услуги", "true"},
      new String[]{"Бытовые услуги", "true"},
      new String[]{"Ремонт и строительство", "true"},
      new String[]{"Красота и уход", "true"},
      new String[]{"Транспортные услуги", "true"},
      new String[]{"IT и компьютерные услуги", "true"},
      new String[]{"Мероприятия и развлечения", "true"}
    );

    for (String[] cat : rootCategories) {
      jdbcTemplate.update(
        "INSERT INTO categories (name, is_service) VALUES (?, ?)",
        cat[0], Boolean.parseBoolean(cat[1])
      );
    }

    Long electronicsId = getCategoryIdByName("Электроника");
    Long clothingId = getCategoryIdByName("Одежда и обувь");
    Long homeId = getCategoryIdByName("Дом и сад");
    Long beautyId = getCategoryIdByName("Красота и здоровье");
    Long sportsId = getCategoryIdByName("Спорт и отдых");
    Long childrenId = getCategoryIdByName("Детские товары");
    Long autoId = getCategoryIdByName("Автотовары");
    Long booksId = getCategoryIdByName("Книги и канцелярия");
    Long hobbyId = getCategoryIdByName("Хобби и творчество");
    Long petsId = getCategoryIdByName("Животные");
    Long tutoringId = getCategoryIdByName("Репетиторство");
    Long educationId = getCategoryIdByName("Образовательные услуги");
    Long householdId = getCategoryIdByName("Бытовые услуги");
    Long repairId = getCategoryIdByName("Ремонт и строительство");
    Long beautyServicesId = getCategoryIdByName("Красота и уход");
    Long transportId = getCategoryIdByName("Транспортные услуги");
    Long itId = getCategoryIdByName("IT и компьютерные услуги");
    Long eventsId = getCategoryIdByName("Мероприятия и развлечения");

    addSubcategory(electronicsId, "Смартфоны", false);
    addSubcategory(electronicsId, "Ноутбуки", false);
    addSubcategory(electronicsId, "Телевизоры", false);
    addSubcategory(electronicsId, "Наушники", false);
    addSubcategory(electronicsId, "Фотоаппараты", false);
    addSubcategory(electronicsId, "Игровые консоли", false);
    addSubcategory(electronicsId, "Умные часы", false);
    addSubcategory(electronicsId, "Планшеты", false);
    addSubcategory(electronicsId, "Компьютеры", false);
    addSubcategory(electronicsId, "Аксессуары", false);
    addSubcategory(electronicsId, "Другое", false);

    addSubcategory(clothingId, "Мужская одежда", false);
    addSubcategory(clothingId, "Женская одежда", false);
    addSubcategory(clothingId, "Детская одежда", false);
    addSubcategory(clothingId, "Обувь", false);
    addSubcategory(clothingId, "Аксессуары", false);
    addSubcategory(clothingId, "Спортивная одежда", false);
    addSubcategory(clothingId, "Верхняя одежда", false);
    addSubcategory(clothingId, "Белье", false);
    addSubcategory(clothingId, "Костюмы", false);
    addSubcategory(clothingId, "Платья", false);
    addSubcategory(clothingId, "Другое", false);

    addSubcategory(homeId, "Мебель", false);
    addSubcategory(homeId, "Текстиль", false);
    addSubcategory(homeId, "Посуда", false);
    addSubcategory(homeId, "Бытовая техника", false);
    addSubcategory(homeId, "Инструменты", false);
    addSubcategory(homeId, "Садовая техника", false);
    addSubcategory(homeId, "Освещение", false);
    addSubcategory(homeId, "Декор", false);
    addSubcategory(homeId, "Хранение", false);
    addSubcategory(homeId, "Кухонные принадлежности", false);
    addSubcategory(homeId, "Другое", false);

    addSubcategory(beautyId, "Косметика", false);
    addSubcategory(beautyId, "Парфюмерия", false);
    addSubcategory(beautyId, "Уходовая косметика", false);
    addSubcategory(beautyId, "БАДы", false);
    addSubcategory(beautyId, "Аптечные товары", false);
    addSubcategory(beautyId, "Гигиена", false);
    addSubcategory(beautyId, "Волосы", false);
    addSubcategory(beautyId, "Лицо", false);
    addSubcategory(beautyId, "Тело", false);
    addSubcategory(beautyId, "Маникюр", false);
    addSubcategory(beautyId, "Другое", false);

    addSubcategory(sportsId, "Фитнес", false);
    addSubcategory(sportsId, "Туризм", false);
    addSubcategory(sportsId, "Велоспорт", false);
    addSubcategory(sportsId, "Зимние виды", false);
    addSubcategory(sportsId, "Водные виды", false);
    addSubcategory(sportsId, "Игровые виды", false);
    addSubcategory(sportsId, "Тренажеры", false);
    addSubcategory(sportsId, "Спортивное питание", false);
    addSubcategory(sportsId, "Одежда", false);
    addSubcategory(sportsId, "Обувь", false);
    addSubcategory(sportsId, "Другое", false);

    addSubcategory(childrenId, "Одежда", false);
    addSubcategory(childrenId, "Обувь", false);
    addSubcategory(childrenId, "Игрушки", false);
    addSubcategory(childrenId, "Коляски", false);
    addSubcategory(childrenId, "Мебель", false);
    addSubcategory(childrenId, "Питание", false);
    addSubcategory(childrenId, "Гигиена", false);
    addSubcategory(childrenId, "Творчество", false);
    addSubcategory(childrenId, "Книги", false);
    addSubcategory(childrenId, "Школа", false);
    addSubcategory(childrenId, "Другое", false);

    addSubcategory(autoId, "Запчасти", false);
    addSubcategory(autoId, "Шины", false);
    addSubcategory(autoId, "Аккумуляторы", false);
    addSubcategory(autoId, "Масла", false);
    addSubcategory(autoId, "Автохимия", false);
    addSubcategory(autoId, "Аксессуары", false);
    addSubcategory(autoId, "Аудио", false);
    addSubcategory(autoId, "Безопасность", false);
    addSubcategory(autoId, "Уход", false);
    addSubcategory(autoId, "Инструменты", false);
    addSubcategory(autoId, "Другое", false);

    addSubcategory(booksId, "Художественная литература", false);
    addSubcategory(booksId, "Научная литература", false);
    addSubcategory(booksId, "Детская литература", false);
    addSubcategory(booksId, "Учебная литература", false);
    addSubcategory(booksId, "Канцелярия", false);
    addSubcategory(booksId, "Бумага", false);
    addSubcategory(booksId, "Письменные принадлежности", false);
    addSubcategory(booksId, "Офисные товары", false);
    addSubcategory(booksId, "Творчество", false);
    addSubcategory(booksId, "Подарки", false);
    addSubcategory(booksId, "Другое", false);

    addSubcategory(hobbyId, "Рукоделие", false);
    addSubcategory(hobbyId, "Рисование", false);
    addSubcategory(hobbyId, "Музыка", false);
    addSubcategory(hobbyId, "Фотография", false);
    addSubcategory(hobbyId, "Коллекционирование", false);
    addSubcategory(hobbyId, "Моделирование", false);
    addSubcategory(hobbyId, "Садоводство", false);
    addSubcategory(hobbyId, "Кулинария", false);
    addSubcategory(hobbyId, "Игры", false);
    addSubcategory(hobbyId, "Пазлы", false);
    addSubcategory(hobbyId, "Другое", false);

    addSubcategory(petsId, "Собаки", false);
    addSubcategory(petsId, "Кошки", false);
    addSubcategory(petsId, "Птицы", false);
    addSubcategory(petsId, "Рыбы", false);
    addSubcategory(petsId, "Грызуны", false);
    addSubcategory(petsId, "Корм", false);
    addSubcategory(petsId, "Аксессуары", false);
    addSubcategory(petsId, "Здоровье", false);
    addSubcategory(petsId, "Игрушки", false);
    addSubcategory(petsId, "Переноски", false);
    addSubcategory(petsId, "Другое", false);

    addSubcategory(tutoringId, "Математика", true);
    addSubcategory(tutoringId, "Физика", true);
    addSubcategory(tutoringId, "Химия", true);
    addSubcategory(tutoringId, "Английский язык", true);
    addSubcategory(tutoringId, "Русский язык", true);
    addSubcategory(tutoringId, "История", true);
    addSubcategory(tutoringId, "Программирование", true);
    addSubcategory(tutoringId, "Музыка", true);
    addSubcategory(tutoringId, "Живопись", true);
    addSubcategory(tutoringId, "Подготовка к школе", true);
    addSubcategory(tutoringId, "Другое", true);

    addSubcategory(educationId, "Курсы", true);
    addSubcategory(educationId, "Семинары", true);
    addSubcategory(educationId, "Тренинги", true);
    addSubcategory(educationId, "Мастер-классы", true);
    addSubcategory(educationId, "Вебинары", true);
    addSubcategory(educationId, "Консультации", true);
    addSubcategory(educationId, "Языковые курсы", true);
    addSubcategory(educationId, "Профориентация", true);
    addSubcategory(educationId, "Другое", true);

    addSubcategory(householdId, "Уборка", true);
    addSubcategory(householdId, "Стирка и глажка", true);
    addSubcategory(householdId, "Приготовление еды", true);
    addSubcategory(householdId, "Уход за детьми", true);
    addSubcategory(householdId, "Уход за животными", true);
    addSubcategory(householdId, "Доставка", true);
    addSubcategory(householdId, "Вывоз мусора", true);
    addSubcategory(householdId, "Химчистка", true);
    addSubcategory(householdId, "Ремонт одежды", true);
    addSubcategory(householdId, "Уход за растениями", true);
    addSubcategory(householdId, "Другое", true);

    addSubcategory(repairId, "Электрика", true);
    addSubcategory(repairId, "Сантехника", true);
    addSubcategory(repairId, "Отделочные работы", true);
    addSubcategory(repairId, "Мебель на заказ", true);
    addSubcategory(repairId, "Ремонт техники", true);
    addSubcategory(repairId, "Строительство", true);
    addSubcategory(repairId, "Дизайн интерьера", true);
    addSubcategory(repairId, "Проектирование", true);
    addSubcategory(repairId, "Ремонт мебели", true);
    addSubcategory(repairId, "Установка оборудования", true);
    addSubcategory(repairId, "Другое", true);

    addSubcategory(beautyServicesId, "Парикмахерские услуги", true);
    addSubcategory(beautyServicesId, "Косметология", true);
    addSubcategory(beautyServicesId, "Маникюр", true);
    addSubcategory(beautyServicesId, "Педикюр", true);
    addSubcategory(beautyServicesId, "Массаж", true);
    addSubcategory(beautyServicesId, "СПА-услуги", true);
    addSubcategory(beautyServicesId, "Тату и пирсинг", true);
    addSubcategory(beautyServicesId, "Визаж", true);
    addSubcategory(beautyServicesId, "Брови и ресницы", true);
    addSubcategory(beautyServicesId, "Другое", true);

    addSubcategory(transportId, "Такси", true);
    addSubcategory(transportId, "Грузоперевозки", true);
    addSubcategory(transportId, "Аренда авто", true);
    addSubcategory(transportId, "Эвакуатор", true);
    addSubcategory(transportId, "Доставка", true);
    addSubcategory(transportId, "Переезд", true);
    addSubcategory(transportId, "Курьерские услуги", true);
    addSubcategory(transportId, "Туристические перевозки", true);
    addSubcategory(transportId, "Другое", true);

    addSubcategory(itId, "Ремонт техники", true);
    addSubcategory(itId, "Программирование", true);
    addSubcategory(itId, "Веб-разработка", true);
    addSubcategory(itId, "Дизайн", true);
    addSubcategory(itId, "Администрирование", true);
    addSubcategory(itId, "Консультации", true);
    addSubcategory(itId, "Обучение", true);
    addSubcategory(itId, "Создание сайтов", true);
    addSubcategory(itId, "SEO-оптимизация", true);
    addSubcategory(itId, "Настройка ПО", true);
    addSubcategory(itId, "Другое", true);

    addSubcategory(eventsId, "Организация праздников", true);
    addSubcategory(eventsId, "Аниматоры", true);
    addSubcategory(eventsId, "Фотографы", true);
    addSubcategory(eventsId, "Видеографы", true);
    addSubcategory(eventsId, "Музыкальное сопровождение", true);
    addSubcategory(eventsId, "Кейтеринг", true);
    addSubcategory(eventsId, "Декор", true);
    addSubcategory(eventsId, "Шоу-программы", true);
    addSubcategory(eventsId, "Квесты", true);
    addSubcategory(eventsId, "Экскурсии", true);
    addSubcategory(eventsId, "Другое", true);

    log.info("Все категории и подкатегории добавлены");
  }

  private Long getCategoryIdByName(String name) {
    return jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name = ?", Long.class, name);
  }

  private void addSubcategory(Long parentId, String name, boolean isService) {
    jdbcTemplate.update(
      "INSERT INTO categories (name, parent_id, is_service) VALUES (?, ?, ?)",
      name, parentId, isService
    );
  }

  private void initializeTags() {
    log.info("Заполняем теги...");

    List<String> tagNames = Arrays.asList(
      "Бренд", "Цвет", "Материал", "Состояние",
      "Гарантия", "Страна производства", "Сезон", "Стиль", "Размер", "Вес"
    );

    for (String tagName : tagNames) {
      jdbcTemplate.update("INSERT INTO tags (name) VALUES (?)", tagName);
    }

    addTagValues("Бренд", Arrays.asList(
      "Apple", "Samsung", "Xiaomi", "Sony", "LG", "Nike", "Adidas",
      "Zara", "H&M", "Bosch", "Philips", "Canon", "Nikon", "Lenovo",
      "HP", "Dell", "Asus", "Microsoft", "Intel", "AMD"
    ));

    addTagValues("Цвет", Arrays.asList(
      "Черный", "Белый", "Серый", "Красный", "Синий", "Зеленый",
      "Желтый", "Оранжевый", "Фиолетовый", "Розовый", "Коричневый",
      "Бежевый", "Голубой", "Серебристый", "Золотой", "Хаки",
      "Бордовый", "Бирюзовый", "Сиреневый", "Мятный"
    ));

    addTagValues("Материал", Arrays.asList(
      "Пластик", "Металл", "Стекло", "Дерево", "Керамика", "Хлопок",
      "Шерсть", "Лен", "Шелк", "Кожа", "Замша", "Джинса", "Флис",
      "Нейлон", "Полиэстер", "Резина", "Бумага", "Картон", "Акрил", "Велюр"
    ));

    addTagValues("Состояние", Arrays.asList(
      "Новое", "Б/у отличное", "Б/у хорошее", "Б/у удовлетворительное",
      "Требует ремонта", "На запчасти", "Как новое", "Следы использования"
    ));

    addTagValues("Гарантия", Arrays.asList(
      "1 месяц", "3 месяца", "6 месяцев", "1 год", "2 года", "3 года", "5 лет", "Без гарантии"
    ));

    addTagValues("Страна производства", Arrays.asList(
      "Россия", "Китай", "США", "Германия", "Япония", "Корея", "Италия", "Франция"
    ));

    addTagValues("Сезон", Arrays.asList(
      "Весна", "Лето", "Осень", "Зима", "Демисезон", "Круглогодичный", "Всесезонный", "Пляжный"
    ));

    addTagValues("Стиль", Arrays.asList(
      "Классический", "Современный", "Минимализм", "Хай-тек", "Винтаж",
      "Ретро", "Лофт", "Спортивный", "Деловой", "Повседневный"
    ));

    addTagValues("Размер", Arrays.asList(
      "XS", "S", "M", "L", "XL", "XXL", "3XL", "4XL"
    ));

    addTagValues("Вес", Arrays.asList(
      "до 1 кг", "1-3 кг", "3-5 кг", "5-10 кг", "10-20 кг", "более 20 кг"
    ));

    log.info("Все теги и их значения добавлены");
  }

  private void addTagValues(String tagName, List<String> values) {
    Long tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, tagName);

    for (String value : values) {
      jdbcTemplate.update("INSERT INTO tag_values (tag_id, value) VALUES (?, ?)", tagId, value);
    }
  }

  public static byte[] fileToBytes(String filePath) throws IOException {
    File file = new File(filePath);

    if (!file.exists()) {
      throw new IOException("File not found: " + filePath);
    }

    byte[] bytes = new byte[(int) file.length()];

    try (FileInputStream fis = new FileInputStream(file)) {
      int bytesRead = fis.read(bytes);
      if (bytesRead != bytes.length) {
        throw new IOException("Failed to read complete file");
      }
    }

    return bytes;
  }

  private byte[] loadPhoto() {
    // Пробуем несколько путей
    String[] paths = {
      "src/main/resources/photo/кот.jpg",
      "src/main/resources/photo/cat.jpg",
      "src/main/resources/static/images/кот.jpg",
      "photo/кот.jpg"
    };

    for (String path : paths) {
      try {
        File file = new File(path);
        log.info("Checking path: {} (exists: {})", file.getAbsolutePath(), file.exists());

        if (file.exists() && file.length() > 0) {
          byte[] bytes = fileToBytes(path);
          log.info("✅ Photo loaded from: {}, size: {} bytes", path, bytes.length);
          return bytes;
        }
      } catch (IOException e) {
        log.debug("Failed to load from {}: {}", path, e.getMessage());
      }
    }

    log.error("❌ No photo file found in any of the checked paths");
    return null;
  }

  private record RegularUserSeed(
      String email,
      String name,
      String password,
      String studyProgram,
      int course,
      String fullAddress
  ) {}

  private record TestAdSeed(
      String ownerName,
      String authorEmail,
      String title,
      String description,
      Category category,
      String subcategory,
      Condition condition,
      int price,
      String location,
      String imagePath,
      List<String> tags
  ) {}
}
