package com.mipt.portal.infrastructure.database;

import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.service.AnnouncementService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TestData {
  static AnnouncementService adsService;
  static AnnouncementRepository adsRepository;

  /*
  static {
    try {
      adsRepository = new AnnouncementRepository();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

   */

  public TestData() throws SQLException {
  }

  /*
  private static void start() {
    adsRepository.createTables();
    adsRepository.insertData();
    generateTestAds();
    adsRepository.insertDataComments();
  }

   */

  private static void generateTestAds() {
    try {
      // Анастасия Шабунина
      Long userId1 = adsService.getUserIdByEmail("shabunina.ao@phystech.edu");
      if (userId1 != null) {
        testCreateAd(userId1, "MacBook Pro 13\" 2020", "Отличный MacBook в идеальном состоянии",
                Category.ELECTRONICS, Condition.NEW, 75000, AdStatus.ACTIVE);
        testCreateAd(userId1, "Учебник по матану", "Сборник задач за 1 курс", Category.BOOKS,
                Condition.NEW, 500, AdStatus.ACTIVE);
        testCreateAd(userId1, "Настольная лампа", "Светодиодная лампа с регулировкой",
                Category.ELECTRONICS, Condition.USED, 1200, AdStatus.DRAFT);
        testCreateAd(userId1, "Калькулятор Casio", "Инженерный калькулятор", Category.ELECTRONICS,
                Condition.BROKEN, 800, AdStatus.UNDER_MODERATION);
      }

      // Мария Соколова
      Long userId2 = adsService.getUserIdByEmail("ivanov.ii@phystech.edu");
      if (userId2 != null) {
        testCreateAd(userId2, "Учебник по физике", "Курс общей физики Ландсберга", Category.BOOKS,
                Condition.USED, 1500, AdStatus.ACTIVE);
        testCreateAd(userId2, "Микроскоп школьный", "Детский микроскоп для начинающих",
                Category.CHILDREN,
                Condition.USED, 2000, AdStatus.ACTIVE);
        testCreateAd(userId2, "Рюкзак студенческий", "Вместительный рюкзак для ноутбука",
                Category.OTHER, Condition.USED, 800, AdStatus.DRAFT);
      }

      // Дмитрий Орлов
      Long userId3 = adsService.getUserIdByEmail("orlov.ka@phystech.edu");
      if (userId3 != null) {
        testCreateAd(userId3, "Игровой компьютер", "Gaming PC для учебы и игр",
                Category.ELECTRONICS,
                Condition.USED, 45000, AdStatus.ACTIVE);
        testCreateAd(userId3, "Клавиатура механическая", "Mechanical keyboard с RGB",
                Category.ELECTRONICS, Condition.NEW, 3500, AdStatus.ACTIVE);
        testCreateAd(userId3, "Стул офисный", "Офисный стул с регулировкой", Category.HOME,
                Condition.NEW, 2500, AdStatus.UNDER_MODERATION);
        testCreateAd(userId3, "Книги по программированию", "Java, Python, Algorithms",
                Category.BOOKS,
                Condition.USED, 1200, AdStatus.DRAFT);
      }

      // Валерия Новикова
      Long userId4 = adsService.getUserIdByEmail("novikova.vv@phystech.edu");
      if (userId4 != null) {
        testCreateAd(userId4, "Микроскоп лабораторный", "Профессиональный для исследований",
                Category.OTHER, Condition.NEW, 15000, AdStatus.ACTIVE);
        testCreateAd(userId4, "Набор реактивов", "Для студенческих опытов", Category.OTHER,
                Condition.BROKEN, 3000, AdStatus.ACTIVE);
        testCreateAd(userId4, "Лабораторный халат", "Белый халат размер M", Category.CLOTHING,
                Condition.USED, 500, AdStatus.UNDER_MODERATION);
      }

      System.out.println("✅ Тестовые объявления созданы!");

    } catch (Exception e) {
      System.err.println("❌ Ошибка: " + e.getMessage());
      e.printStackTrace();
    }
  }

  static private void testCreateAd(Long userId, String title, String description, Category category,
                                   Condition condition, int price, AdStatus status) throws SQLException {
    /*
    Announcement ad = new Announcement(title, description, category, condition, price,
        "МФТИ, Долгопрудный", userId);
    ad.setStatus(status);
    ad.setId(adsService.getAdsRepository().saveAd(ad));
    adsService.getUserService().addAnnouncementId(ad.getUserId(), ad.getId());

     */
  }

  public static void uploadAllPhotos() {
    try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/myproject",
            "myuser",
            "mypassword")) {

      String basePath = "src/main/resources/jpg/";

      Map<Integer, String[]> photoMapping = new HashMap<>();

      //
      photoMapping.put(1, new String[]{basePath + "макбук.jpg"});
      photoMapping.put(2, new String[]{basePath + "математика.jpg"});
      photoMapping.put(4, new String[]{basePath + "калькулятор.jpg"});
      photoMapping.put(5, new String[]{basePath + "физика.jpg"});
      photoMapping.put(6, new String[]{basePath + "микроскоп.jpg"});
      photoMapping.put(7, new String[]{basePath + "рюкзак_студенческий.jpg"});
      photoMapping.put(8, new String[]{basePath + "компьютер.jpg"});
      photoMapping.put(9, new String[]{basePath + "клавиатура.jpg"});
      photoMapping.put(10, new String[]{basePath + "стул.jpg"});
      photoMapping.put(13, new String[]{basePath + "набор_реактивов.jpg"});
      photoMapping.put(14, new String[]{basePath + "халат.jpg"});

      for (Map.Entry<Integer, String[]> entry : photoMapping.entrySet()) {
        int adId = entry.getKey();
        String[] photoPaths = entry.getValue();

        /*
        try (MediaManager mediaManager = new MediaManager(conn, adId)) {
          for (String path : photoPaths) {
            try {
              mediaManager.addPhoto(path);
            } catch (IOException e) {
              System.err.println("" + path);
            }
          }
          mediaManager.saveToDB();
        } catch (Exception e) {
          System.err.println("Ошибка для объявления " + adId + ": " + e.getMessage());
        }

         */
      }

    } catch (SQLException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    //start();
    uploadAllPhotos();
  }
}