package com.mipt.portal.service;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.repository.AnnouncementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

  @Mock private AnnouncementRepository announcementRepository;
  @InjectMocks private MediaService service;

  @Test
  void fileToBytes_readsFile(@TempDir Path tempDir) throws IOException {
    Path f = tempDir.resolve("a.txt");
    Files.writeString(f, "hello");
    byte[] data = service.fileToBytes(f.toString());
    assertThat(new String(data)).isEqualTo("hello");
  }

  @Test
  void multipartFileToBytes_readsContent() throws IOException {
    MockMultipartFile mpf = new MockMultipartFile("photo", "p.jpg", "image/jpeg", "abc".getBytes());
    assertThat(service.multipartFileToBytes(mpf)).isEqualTo("abc".getBytes());
  }

  @Test
  void savePhoto_setsPhotoBytes() {
    Announcement ad = new Announcement();
    ad.setId(1L);
    when(announcementRepository.findById(1L)).thenReturn(Optional.of(ad));
    service.savePhoto(1L, new byte[]{1, 2, 3});
    assertThat(ad.getPhoto()).containsExactly(1, 2, 3);
    verify(announcementRepository).save(ad);
  }

  @Test
  void savePhoto_throwsWhenAdMissing() {
    when(announcementRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.savePhoto(1L, new byte[]{1}))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("не найдено");
  }

  @Test
  void deletePhoto_clearsBytes() {
    Announcement ad = new Announcement();
    ad.setId(1L);
    ad.setPhoto(new byte[]{9, 9, 9});
    when(announcementRepository.findById(1L)).thenReturn(Optional.of(ad));
    service.deletePhoto(1L);
    assertThat(ad.getPhoto()).isEmpty();
    verify(announcementRepository).save(ad);
  }

  @Test
  void deletePhoto_throwsWhenMissing() {
    when(announcementRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.deletePhoto(1L))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void getPhoto_returnsPhoto() {
    Announcement ad = new Announcement();
    ad.setPhoto(new byte[]{4});
    when(announcementRepository.findById(1L)).thenReturn(Optional.of(ad));
    assertThat(service.getPhoto(1L)).containsExactly(4);
  }

  @Test
  void getPhoto_throwsWhenMissing() {
    when(announcementRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getPhoto(1L))
        .isInstanceOf(RuntimeException.class);
  }
}
