package com.mipt.portal.annoucementContent.media;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class MediaService {
//
//  private final AnnouncementRepository announcementRepository;
//
//  @Transactional
//  public void addPhoto(Long announcementId, MultipartFile file) throws IOException {
//    log.info("Добавление фото к объявлению ID: {}", announcementId);
//
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    byte[] fileData = file.getBytes();
//
//    // Получаем текущие фото
//    byte[][] currentPhotos = announcement.getPhotos();
//    if (currentPhotos == null) {
//      currentPhotos = new byte[0][];
//    }
//
//    // Создаем новый массив с добавленным фото
//    byte[][] newPhotos = Arrays.copyOf(currentPhotos, currentPhotos.length + 1);
//    newPhotos[newPhotos.length - 1] = fileData;
//
//    announcement.setPhotos(newPhotos);
//    announcementRepository.save(announcement);
//
//    log.info("✅ Фото добавлено. Всего фото: {}", newPhotos.length);
//  }
//
//  @Transactional
//  public void addPhoto(Long announcementId, byte[] fileData) {
//    log.info("Добавление фото (byte[]) к объявлению ID: {}", announcementId);
//
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    byte[][] currentPhotos = announcement.getPhotos();
//    if (currentPhotos == null) {
//      currentPhotos = new byte[0][];
//    }
//
//    byte[][] newPhotos = Arrays.copyOf(currentPhotos, currentPhotos.length + 1);
//    newPhotos[newPhotos.length - 1] = fileData;
//
//    announcement.setPhotos(newPhotos);
//    announcementRepository.save(announcement);
//
//    log.info("✅ Фото добавлено. Всего фото: {}", newPhotos.length);
//  }
//
//  @Transactional(readOnly = true)
//  public List<byte[]> getPhotos(Long announcementId) {
//    log.info("Получение фото для объявления ID: {}", announcementId);
//
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    byte[][] photos = announcement.getPhotos();
//    if (photos == null) {
//      return new ArrayList<>();
//    }
//
//    return Arrays.asList(photos);
//  }
//
//  @Transactional(readOnly = true)
//  public byte[] getPhotoByIndex(Long announcementId, int index) {
//    log.info("Получение фото с индексом {} для объявления ID: {}", index, announcementId);
//
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    byte[][] photos = announcement.getPhotos();
//    if (photos == null || index < 0 || index >= photos.length) {
//      throw new RuntimeException("Фото с индексом " + index + " не найдено");
//    }
//
//    return photos[index];
//  }
//
//  @Transactional
//  public void deletePhoto(Long announcementId, int index) {
//    log.info("Удаление фото с индексом {} для объявления ID: {}", index, announcementId);
//
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    byte[][] photos = announcement.getPhotos();
//    if (photos == null || index < 0 || index >= photos.length) {
//      throw new RuntimeException("Фото с индексом " + index + " не найдено");
//    }
//
//    // Создаем новый массив без удаляемого фото
//    byte[][] newPhotos = new byte[photos.length - 1][];
//    int newIndex = 0;
//    for (int i = 0; i < photos.length; i++) {
//      if (i != index) {
//        newPhotos[newIndex++] = photos[i];
//      }
//    }
//
//    announcement.setPhotos(newPhotos);
//    announcementRepository.save(announcement);
//
//    log.info("✅ Фото удалено. Осталось фото: {}", newPhotos.length);
//  }
//
//  @Transactional
//  public void deleteAllPhotos(Long announcementId) {
//    log.info("Удаление всех фото для объявления ID: {}", announcementId);
//
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    announcement.setPhotos(new byte[0][]);
//    announcementRepository.save(announcement);
//
//    log.info("✅ Все фото удалены");
//  }
//
//  @Transactional(readOnly = true)
//  public int getPhotosCount(Long announcementId) {
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + announcementId));
//
//    byte[][] photos = announcement.getPhotos();
//    return photos == null ? 0 : photos.length;
//  }
//
//  // Валидация изображения (как в старом коде)
//  private boolean isValidImageData(byte[] data) {
//    if (data == null || data.length < 100) return false;
//
//    if (data.length >= 3) {
//      // JPEG: FF D8 FF
//      if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
//        return true;
//      }
//      // PNG: 89 50 4E 47
//      if (data.length >= 4 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 &&
//        data[2] == 0x4E && data[3] == 0x47) {
//        return true;
//      }
//    }
//    return data.length > 1000;
//  }
//}