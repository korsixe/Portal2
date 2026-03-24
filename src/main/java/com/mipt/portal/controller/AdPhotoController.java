package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdPhotoController {

  private final AnnouncementService announcementService;

  @GetMapping("/ad-photo")
  public ResponseEntity<byte[]> getPhoto(
    @RequestParam("adId") Long adId,
    @RequestParam(value = "photoIndex", defaultValue = "0") int photoIndex) {

    try {
      Announcement ad = announcementService.findById(adId);

      if (ad == null) {
        log.warn("Ad not found: {}", adId);
        return ResponseEntity.notFound().build();
      }

      // Получаем фото из поля photo
      byte[] photoData = ad.getPhoto();

      if (photoData == null || photoData.length == 0) {
        log.warn("No photo found for ad: {}", adId);
        return ResponseEntity.notFound().build();
      }

      // Игнорируем photoIndex, так как только одно фото
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.IMAGE_JPEG);
      headers.setContentLength(photoData.length);

      log.info("Serving photo for ad: {}, size: {} bytes", adId, photoData.length);

      return new ResponseEntity<>(photoData, headers, HttpStatus.OK);

    } catch (Exception e) {
      log.error("Error loading photo for ad {}: {}", adId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }
}