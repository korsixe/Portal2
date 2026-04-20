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

  private static final String TEST_PHOTO_BASE64 =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
  private static final String TEST_PHOTO_BASE64_ALT_1 =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M/wHwAEAQH/cetH5QAAAABJRU5ErkJggg==";
  private static final String TEST_PHOTO_BASE64_ALT_2 =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/58BAgMDAwD0KQf7w2Y3WQAAAABJRU5ErkJggg==";

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        initializeCategoriesAndTags();

        User admin = createTestUser("admin.test@phystech.edu", "admin", Role.ADMIN);
        User moderator = createTestUser("moderator.test@phystech.edu", "moderator", Role.MODERATOR);
        User user = createTestUser("user.test@phystech.edu", "user", Role.USER);

        createSampleAnnouncements(admin, moderator, user);
        ensureRegularUserHasApprovedAds(user);
        ensureSeededAnnouncementsHavePhotos();
    }

    private void ensureRegularUserHasApprovedAds(User regularUser) {
        if (regularUser == null || regularUser.getId() == null) {
            return;
        }

        List<Announcement> existingUserAds = announcementRepository.findAllByAuthorId(regularUser.getId());

        String[] titles = {
            "iPhone 12 128GB, аккумулятор 89%",
            "Наушники Sony WH-1000XM4, полный комплект"
        };
        String[] descriptions = {
            "Отличное состояние, всегда в чехле. Есть чек и коробка.",
            "Шумоподавление работает отлично, без дефектов, продаю из-за апгрейда."
        };
        String[] subcategories = {"Смартфоны", "Наушники"};
        int[] prices = {36500, 14900};
        byte[][] photos = {
            decodeBase64Photo(TEST_PHOTO_BASE64_ALT_1),
            decodeBase64Photo(TEST_PHOTO_BASE64_ALT_2)
        };

        int toCreate = 2;
        for (int i = 0; i < toCreate; i++) {
            final String desiredTitle = titles[i];
            boolean alreadyExists = existingUserAds.stream()
                .anyMatch(ad -> desiredTitle.equals(ad.getTitle()));
            if (alreadyExists) {
                continue;
            }

            Announcement ad = new Announcement();
            ad.setTitle(desiredTitle);
            ad.setDescription(descriptions[i]);
            ad.setCategory(Category.ELECTRONICS);
            ad.setSubcategory(subcategories[i]);
            ad.setCondition(Condition.USED);
            ad.setPrice(prices[i]);
            ad.setLocation(i == 0 ? "Москва, Бауманская" : "Долгопрудный, МФТИ");
            ad.setAuthorId(regularUser.getId());
            ad.setStatus(AdStatus.ACTIVE);
            List<String> tags = i == 0
                ? List.of("apple", "iphone", "б/у")
                : List.of("sony", "headphones", "noise-canceling");
            ad.setTags(tags);
            ad.setTagsCount(tags.size());
            ad.setPhoto(photos[i]);
            announcementRepository.save(ad);
        }

        log.info("Для пользователя {} создано {} одобренных объявлений", regularUser.getEmail(), toCreate);
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
        applyTestUserProfile(user, role);
        return userRepository.save(user);
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

    private void createSampleAnnouncements(User admin, User moderator, User regularUser) throws IOException {
        if (announcementRepository.count() > 0) {
            return;
        }

        Long adminId = admin != null ? admin.getId() : null;
        Long moderatorId = moderator != null ? moderator.getId() : null;
        Long userId = regularUser != null ? regularUser.getId() : null;

        Announcement pending = new Announcement();
        pending.setTitle("MacBook Pro 14\" 2023");
        pending.setDescription("Состояние отличное, есть чек. Жду проверки модерации.");
        pending.setCategory(Category.ELECTRONICS);
        pending.setSubcategory("Ноутбуки");
        pending.setCondition(Condition.USED);
        pending.setPrice(145000);
        pending.setLocation("Москва, МФТИ");
        pending.setAuthorId(userId);
        pending.setStatus(AdStatus.UNDER_MODERATION);
        pending.setTags(List.of("macbook", "apple", "m1"));
        pending.setTagsCount(3);
        pending.setPhoto(null);

      Announcement active = new Announcement();
      active.setTitle("Смартфон Pixel 7a");
      active.setDescription("Официальная версия, полный комплект. Уже одобрено.");
      active.setCategory(Category.ELECTRONICS);
      active.setSubcategory("Смартфоны");
      active.setCondition(Condition.USED);
      active.setPrice(32000);
      active.setLocation("Долгопрудный");
      active.setAuthorId(moderatorId);
      active.setStatus(AdStatus.ACTIVE);
      active.setTags(List.of("google", "pixel"));
      active.setTagsCount(2);

      byte[] testPhoto = getTestPhotoBytes();
      pending.setPhoto(testPhoto);
      active.setPhoto(testPhoto);

        Announcement rejected = new Announcement();
        rejected.setTitle("Продам учебники");
        rejected.setDescription("Сборник задач по матанализу. Предыдущее объявление отклонено.");
        rejected.setCategory(Category.BOOKS);
        rejected.setSubcategory("Учебники");
        rejected.setCondition(Condition.USED);
        rejected.setPrice(1500);
        rejected.setLocation("Москва, ВДНХ");
        rejected.setAuthorId(adminId);
        rejected.setStatus(AdStatus.REJECTED);
        rejected.setTags(List.of("книги", "матан"));
        rejected.setTagsCount(2);
        rejected.setPhoto(testPhoto);

        announcementRepository.saveAll(List.of(pending, active, rejected));
    }

  private byte[] getTestPhotoBytes() {
    return decodeBase64Photo(TEST_PHOTO_BASE64);
  }

  private byte[] decodeBase64Photo(String value) {
    try {
      byte[] photoBytes = Base64.getDecoder().decode(value);
      log.info("✅ Test photo prepared, size: {} bytes", photoBytes.length);
      return photoBytes;
    } catch (Exception e) {
      log.error("Failed to decode test photo: {}", e.getMessage());
      return null;
    }
  }

  private void ensureSeededAnnouncementsHavePhotos() {
    byte[] testPhoto = getTestPhotoBytes();
    if (testPhoto == null || testPhoto.length == 0) {
      return;
    }

    List<String> seededTitles = List.of(
      "MacBook Pro 14\" 2023",
      "Смартфон Pixel 7a",
      "Продам учебники"
    );

    List<Announcement> allAds = announcementRepository.findAll();
    for (Announcement ad : allAds) {
      if (seededTitles.contains(ad.getTitle()) && (ad.getPhoto() == null || ad.getPhoto().length == 0)) {
        ad.setPhoto(testPhoto);
        announcementRepository.save(ad);
        log.info("Добавлено тестовое фото для объявления ID={} title='{}'", ad.getId(), ad.getTitle());
      }
    }
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
}
