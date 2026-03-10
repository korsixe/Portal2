package com.mipt.portal.annoucementContent.media;

import com.mipt.portal.annoucementContent.MediaManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.SQLException;

@SpringBootApplication
public class MediaTest {

  public static void main(String[] args) throws SQLException, IOException {
    /*
    // Запускаем Spring контекст
    ConfigurableApplicationContext context = SpringApplication.run(MediaTest.class, args);

    // Получаем бин из контекста Spring
    MediaManager mediaManager = context.getBean(MediaManager.class);

    // Работаем с менеджером
    mediaManager.addPhoto(1, "/Users/elizavetaorlova/Downloads/кот_2.jpg");
    mediaManager.showPhotos(1);
    System.out.println(mediaManager.getPhotosCount(1));

    // Сохраняем в папку
    saveToFolder(mediaManager, 1, "/Users/elizavetaorlova/IdeaProjects/Portal1/exported_photos/");

    mediaManager.deleteAllPhotos(1);

    // Закрываем контекст
    context.close();
    */

    System.out.println("MediaTest заглушка: тест пропущен");
  }

  private static void saveToFolder(MediaManager manager, int adId, String folderPath) throws IOException {
    /*
    File folder = new File(folderPath);
    if (folder.exists()) {
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
      }
    }
    folder.mkdirs();

    int photoCount = manager.getPhotosCount(adId);
    for (int i = 0; i < photoCount; i++) {
      byte[] photoData = manager.getPhotoByIndex(adId, i);
      File outputFile = new File(folder, "photo_" + i + ".jpg");
      Files.write(outputFile.toPath(), photoData);
      System.out.println("✅ Сохранено фото: " + outputFile.getPath());
    }
    */
    System.out.println("saveToFolder заглушка: фото не сохраняются");
  }
}