package com.mipt.portal.config;

import com.mipt.portal.repository.CategoryRepository;
import com.mipt.portal.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DataInitializer2 implements CommandLineRunner {

  private final JdbcTemplate jdbcTemplate;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;

  @Override
  @Transactional
  public void run(String... args) {
    Integer categoriesCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
    Integer tagsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tags", Integer.class);

    if (categoriesCount == 0) {
      log.info("Начинаем заполнение категорий...");
      initializeCategories();
      log.info("Категории успешно заполнены!");
    } else {
      log.info("Категории уже существуют, пропускаем инициализацию");
    }

    if (tagsCount == 0) {
      log.info("Начинаем заполнение тегов...");
      initializeTags();
      log.info("Теги успешно заполнены!");
    } else {
      log.info("Теги уже существуют, пропускаем инициализацию");
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
    return jdbcTemplate.queryForObject(
      "SELECT id FROM categories WHERE name = ?", Long.class, name);
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
    Long tagId = jdbcTemplate.queryForObject(
      "SELECT id FROM tags WHERE name = ?", Long.class, tagName);

    for (String value : values) {
      jdbcTemplate.update(
        "INSERT INTO tag_values (tag_id, value) VALUES (?, ?)",
        tagId, value
      );
    }
  }
}