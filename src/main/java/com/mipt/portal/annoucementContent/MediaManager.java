package com.mipt.portal.annoucementContent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaManager {

  private final DataSource dataSource;

  @Transactional
  public void addPhoto(int adId, String filePath) throws IOException {
        /*
        byte[] fileData = Files.readAllBytes(new File(filePath).toPath());
        List<byte[]> currentPhotos = loadPhotosFromDB(adId);
        currentPhotos.add(fileData);
        savePhotosToDB(adId, currentPhotos);
        log.info("✅ Фото добавлено для объявления ID: {} из файла {}", adId, filePath);
        */
    log.info("✅ Фото добавлено (заглушка) для объявления ID: {} из файла {}", adId, filePath);
  }

  @Transactional
  public void addPhoto(int adId, byte[] fileData) {
        /*
        if (fileData != null && fileData.length > 0) {
            List<byte[]> currentPhotos = loadPhotosFromDB(adId);
            currentPhotos.add(fileData);
            savePhotosToDB(adId, currentPhotos);
            log.info("✅ Фото добавлено для объявления ID: {} (байты)", adId);
        }
        */
    log.info("✅ Фото добавлено (заглушка) для объявления ID: {} (байты)", adId);
  }

  @Transactional
  public void addPhoto(int adId, MultipartFile file) throws IOException {
        /*
        if (file != null && !file.isEmpty()) {
            byte[] fileData = file.getBytes();
            List<byte[]> currentPhotos = loadPhotosFromDB(adId);
            currentPhotos.add(fileData);
            savePhotosToDB(adId, currentPhotos);
            log.info("✅ Фото добавлено для объявления ID: {} из MultipartFile", adId);
        }
        */
    log.info("✅ Фото добавлено (заглушка) для объявления ID: {} из MultipartFile", adId);
  }

  @Transactional(readOnly = true)
  public void showPhotos(int adId) {
        /*
        List<byte[]> photos = loadPhotosFromDB(adId);
        for (int i = 0; i < photos.size(); i++) {
            System.out.println(i + ". " + photos.get(i).length + " байт");
        }
        */
    log.info("📸 Показано фото (заглушка) для объявления ID: {}", adId);
  }

  @Transactional
  protected void savePhotosToDB(int adId, List<byte[]> photos) {
        /*
        String sql = "UPDATE ads SET photos = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (photos.isEmpty()) {
                stmt.setNull(1, Types.ARRAY);
            } else {
                byte[][] photosArray = photos.toArray(new byte[0][]);
                Array sqlArray = connection.createArrayOf("bytea", photosArray);
                stmt.setArray(1, sqlArray);
            }

            stmt.setInt(2, adId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Не удалось обновить запись. Возможно, adId=" + adId + " не существует");
            }

            log.info("✅ Сохранено {} фото для объявления ID: {}", photos.size(), adId);

        } catch (SQLException e) {
            log.error("❌ MediaManager.savePhotosToDB() ОШИБКА: {}", e.getMessage());
            throw new RuntimeException("Ошибка при сохранении фото", e);
        }
        */
    log.info("✅ Сохранено {} фото (заглушка) для объявления ID: {}", photos.size(), adId);
  }

  @Transactional(readOnly = true)
  public List<byte[]> loadPhotosFromDB(int adId) {
        /*
        String sql = "SELECT photos FROM ads WHERE id = ?";
        List<byte[]> photos = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, adId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Array photosArray = rs.getArray("photos");
                if (photosArray != null) {
                    Object[] dbPhotos = (Object[]) photosArray.getArray();
                    if (dbPhotos != null) {
                        for (Object photoData : dbPhotos) {
                            if (photoData instanceof byte[]) {
                                byte[] imageData = (byte[]) photoData;
                                if (isValidImageData(imageData)) {
                                    photos.add(imageData);
                                }
                            }
                        }
                    }
                }
            }

            log.info("✅ Загружено {} фото для объявления ID: {}", photos.size(), adId);

        } catch (SQLException e) {
            log.error("❌ MediaManager.loadPhotosFromDB() ОШИБКА: {}", e.getMessage());
            throw new RuntimeException("Ошибка при загрузке фото", e);
        }

        return photos;
        */
    log.info("✅ Загружено фото (заглушка) для объявления ID: {}", adId);
    return new ArrayList<>();
  }

  private boolean isValidImageData(byte[] data) {
        /*
        if (data == null || data.length < 100) return false;

        if (data.length >= 3) {
            if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
                return true;
            }
            if (data.length >= 4 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 &&
                data[2] == 0x4E && data[3] == 0x47) {
                return true;
            }
        }
        return data.length > 1000;
        */
    return false;
  }

  @Transactional
  public void deletePhotoFromDB(int adId, int index) {
        /*
        List<byte[]> photos = loadPhotosFromDB(adId);

        if (index < 0 || index >= photos.size()) {
            throw new IllegalArgumentException("Неверный индекс: " + index);
        }

        photos.remove(index);
        savePhotosToDB(adId, photos);
        log.info("✅ Удалено фото с индексом {} для объявления ID: {}", index, adId);
        */
    log.info("✅ Удалено фото (заглушка) с индексом {} для объявления ID: {}", index, adId);
  }

  @Transactional(readOnly = true)
  public byte[] getPhotoByIndex(int adId, int index) {
        /*
        List<byte[]> photos = loadPhotosFromDB(adId);
        if (index < 0 || index >= photos.size()) {
            throw new IllegalArgumentException("Invalid photo index: " + index);
        }
        return photos.get(index);
        */
    log.info("📸 Получено фото (заглушка) по индексу {} для объявления ID: {}", index, adId);
    return new byte[0];
  }

  @Transactional(readOnly = true)
  public int getPhotosCount(int adId) {
    // return loadPhotosFromDB(adId).size();
    log.info("🔢 Получено количество фото (заглушка) для объявления ID: {}", adId);
    return 0;
  }

  @Transactional
  public void deleteAllPhotos(int adId) {
    // savePhotosToDB(adId, new ArrayList<>());
    // log.info("✅ Удалены все фото для объявления ID: {}", adId);
    log.info("✅ Удалены все фото (заглушка) для объявления ID: {}", adId);
  }

  @Transactional(readOnly = true)
  public List<byte[]> getAllPhotos(int adId) {
    // return loadPhotosFromDB(adId);
    log.info("📸 Получены все фото (заглушка) для объявления ID: {}", adId);
    return new ArrayList<>();
  }

  @Transactional(readOnly = true)
  public List<byte[]> loadAndGetPhotos(int adId) {
    // return loadPhotosFromDB(adId);
    log.info("📸 Загружены и получены все фото (заглушка) для объявления ID: {}", adId);
    return new ArrayList<>();
  }
}