package com.mipt.portal.annoucementContent.media;

import com.mipt.portal.announcement.Announcement;
//import com.mipt.portal.announcement.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

  //private final AnnouncementRepository announcementRepository;

  // 1. Перевод фото в байтовый массив
  public byte[] fileToBytes(String filePath) throws IOException {
    return Files.readAllBytes(Paths.get(filePath));
  }

  // из формы
  public byte[] multipartFileToBytes(MultipartFile file) throws IOException {
    return file.getBytes();
  }

  // 2. Сохранение массива в photo Announcement
//  @Transactional
//  public void savePhoto(Long announcementId, byte[] photoData) {
//    Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new RuntimeException("Объявление не найдено"));
//
//    announcement.setPhoto(photoData);
//    announcementRepository.save(announcement);
//  }

  // 3. Удаление фото
  @Transactional
  public void deletePhoto(Long announcementId) {
//    Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new RuntimeException("Объявление не найдено"));
//    announcement.setPhoto(new byte[0]);
//    announcementRepository.save(announcement);
  }

//  @Transactional(readOnly = true)
//  public byte[] getPhoto(Long announcementId) {
//    Announcement announcement = announcementRepository.findById(announcementId)
//      .orElseThrow(() -> new RuntimeException("Объявление не найдено"));
//    return announcement.getPhoto();
//  }
}