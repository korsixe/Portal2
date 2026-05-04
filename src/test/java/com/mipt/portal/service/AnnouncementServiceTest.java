package com.mipt.portal.service;

import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Comment;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

  @Mock private AnnouncementRepository repository;
  @Mock private UserRepository userRepository;
  @Mock private ModerationHistoryService moderationHistoryService;
  @Mock private AuditService auditService;
  @Mock private CategoryService categoryService;
  @Mock private CommentService commentService;
  @Mock private KafkaMessageService kafkaMessageService;
  @InjectMocks private AnnouncementService service;

  @Test
  void create_setsDefaultsAndPublishes() {
    AnnouncementCreateDto dto = new AnnouncementCreateDto();
    dto.setTitle("Hello");
    dto.setDescription("desc");
    dto.setPrice(100);
    dto.setAuthorId(1L);
    dto.setCategory("ELECTRONICS");
    dto.setSubcategory("phones");
    dto.setLocation("Moscow");
    dto.setCondition("NEW");

    when(repository.save(any(Announcement.class))).thenAnswer(inv -> {
      Announcement a = inv.getArgument(0);
      a.setId(1L);
      return a;
    });

    Announcement result = service.create(dto);
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getStatus()).isEqualTo(AdStatus.DRAFT);
    assertThat(result.getCategory()).isEqualTo(Category.ELECTRONICS);
    assertThat(result.getCondition()).isEqualTo(Condition.NEW);
    verify(kafkaMessageService).sendAnnouncementEvent(eq("announcement.created"), eq("1"), any());
  }

  @Test
  void create_blankCategoryDefaultsToOther() {
    AnnouncementCreateDto dto = new AnnouncementCreateDto();
    dto.setTitle("X");
    dto.setAuthorId(1L);
    dto.setCategory("");
    dto.setCondition("");
    when(repository.save(any(Announcement.class))).thenAnswer(inv -> {
      Announcement a = inv.getArgument(0);
      a.setId(2L);
      return a;
    });
    Announcement r = service.create(dto);
    assertThat(r.getCategory()).isEqualTo(Category.OTHER);
    assertThat(r.getCondition()).isEqualTo(Condition.USED);
  }

  @Test
  void create_invalidConditionFallsBackToUsed() {
    AnnouncementCreateDto dto = new AnnouncementCreateDto();
    dto.setTitle("X");
    dto.setAuthorId(1L);
    dto.setCategory("ELECTRONICS");
    dto.setCondition("INVALID_VALUE");
    when(repository.save(any(Announcement.class))).thenAnswer(inv -> {
      Announcement a = inv.getArgument(0);
      a.setId(3L);
      return a;
    });
    Announcement r = service.create(dto);
    assertThat(r.getCondition()).isEqualTo(Condition.USED);
  }

  @Test
  void create_unrecognizedCategoryFallsBackViaDisplayName() {
    AnnouncementCreateDto dto = new AnnouncementCreateDto();
    dto.setTitle("X");
    dto.setAuthorId(1L);
    // Use a likely-unmatched value to trigger fromDisplayName path
    dto.setCategory("Электроника");
    dto.setCondition("USED");
    when(repository.save(any(Announcement.class))).thenAnswer(inv -> {
      Announcement a = inv.getArgument(0);
      a.setId(4L);
      return a;
    });
    Announcement r = service.create(dto);
    assertThat(r.getCategory()).isNotNull();
  }

  @Test
  void findAllByIds_emptyForNullOrEmpty() {
    assertThat(service.findAllByIds(null)).isEmpty();
    assertThat(service.findAllByIds(List.of())).isEmpty();
  }

  @Test
  void findAllByIds_delegates() {
    Announcement a = new Announcement();
    when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(a));
    assertThat(service.findAllByIds(List.of(1L, 2L))).hasSize(1);
  }

  @Test
  void searchApproved_delegates() {
    AnnouncementFilterDto f = new AnnouncementFilterDto();
    when(repository.searchApproved(f, "createdAt", "DESC")).thenReturn(List.of());
    assertThat(service.searchApproved(f, "createdAt", "DESC")).isEmpty();
  }

  @Test
  void getPendingForModerator_delegates() {
    when(repository.findAllByStatus(AdStatus.UNDER_MODERATION)).thenReturn(List.of(new Announcement()));
    assertThat(service.getPendingForModerator()).hasSize(1);
  }

  @Test
  void findAllByAuthorId_delegates() {
    when(repository.findAllByAuthorId(7L)).thenReturn(List.of(new Announcement()));
    assertThat(service.findAllByAuthorId(7L)).hasSize(1);
  }

  @Test
  void sendToModeration_marksAndPublishes() {
    Announcement ad = new Announcement();
    ad.setId(1L);
    ad.setStatus(AdStatus.DRAFT);
    when(repository.findById(1L)).thenReturn(Optional.of(ad));
    service.sendToModeration(1L);
    assertThat(ad.getStatus()).isEqualTo(AdStatus.UNDER_MODERATION);
    verify(repository).save(ad);
    verify(kafkaMessageService).sendAnnouncementEvent(eq("announcement.sent_to_moderation"), eq("1"), any());
  }

  @Test
  void sendToModeration_skipsWhenAbsent() {
    when(repository.findById(99L)).thenReturn(Optional.empty());
    service.sendToModeration(99L);
    verify(repository, never()).save(any());
  }

  @Test
  void changeStatus_persistsRecordsHistoryAndAudits() {
    Announcement ad = new Announcement();
    ad.setId(1L);
    ad.setStatus(AdStatus.UNDER_MODERATION);
    when(repository.findById(1L)).thenReturn(Optional.of(ad));
    when(repository.save(ad)).thenReturn(ad);

    Optional<Announcement> r = service.changeStatus(1L, AdStatus.ACTIVE, 7L, "good");
    assertThat(r).isPresent();
    assertThat(ad.getStatus()).isEqualTo(AdStatus.ACTIVE);
    verify(moderationHistoryService).record(eq(1L), eq(7L), eq(AdStatus.UNDER_MODERATION), eq(AdStatus.ACTIVE), eq("good"));
    verify(auditService).logAdminAction(eq(7L), any(), any(), any(), eq(1L), anyString());
    verify(kafkaMessageService).sendAnnouncementEvent(eq("announcement.status_changed"), eq("1"), any());
  }

  @Test
  void changeStatus_emptyWhenAbsent() {
    when(repository.findById(99L)).thenReturn(Optional.empty());
    assertThat(service.changeStatus(99L, AdStatus.ACTIVE, 1L, null)).isEmpty();
  }

  @Test
  void changeStatus_handlesNullReason() {
    Announcement ad = new Announcement();
    ad.setStatus(AdStatus.DRAFT);
    when(repository.findById(1L)).thenReturn(Optional.of(ad));
    when(repository.save(ad)).thenReturn(ad);
    assertThat(service.changeStatus(1L, AdStatus.ACTIVE, 7L, null)).isPresent();
  }

  @Test
  void getUserIdByEmail_returnsId() {
    User u = new User();
    u.setId(1L);
    when(userRepository.findByEmail("u@x.com")).thenReturn(Optional.of(u));
    assertThat(service.getUserIdByEmail("u@x.com")).isEqualTo(1L);
  }

  @Test
  void getUserIdByEmail_nullWhenAbsent() {
    when(userRepository.findByEmail("missing")).thenReturn(Optional.empty());
    assertThat(service.getUserIdByEmail("missing")).isNull();
  }

  @Test
  void findById_returnsOrNull() {
    Announcement a = new Announcement();
    when(repository.findById(1L)).thenReturn(Optional.of(a));
    assertThat(service.findById(1L)).isSameAs(a);
    when(repository.findById(2L)).thenReturn(Optional.empty());
    assertThat(service.findById(2L)).isNull();
  }

  @Test
  void categoryDelegates_passThrough() {
    when(categoryService.getAllCategories()).thenReturn(List.of(Map.of("id", 1)));
    when(categoryService.getSubcategoriesByCategory(1L)).thenReturn(List.of());
    when(categoryService.getTagsWithValues()).thenReturn(List.of());
    when(categoryService.getTagsForAd(1L)).thenReturn(List.of());

    assertThat(service.getAllCategories()).hasSize(1);
    assertThat(service.getSubcategoriesByCategory(1L)).isEmpty();
    assertThat(service.getTagsWithValues()).isEmpty();
    assertThat(service.getTagsForAd(1L)).isEmpty();
  }

  @Test
  void saveAdTags_delegates() {
    List<Map<String, Object>> tags = List.of(Map.of("k", "v"));
    service.saveAdTags(1L, tags);
    verify(categoryService).saveAdTags(1L, tags);
  }

  @Test
  void addComment_delegates() {
    service.addComment(1L, 2L, "Ivan", "hi");
    verify(commentService).createComment(1L, 2L, "Ivan", "hi");
  }

  @Test
  void getCommentsByAdId_delegates() {
    when(commentService.getCommentsByAdId(1L)).thenReturn(List.of(new Comment()));
    assertThat(service.getCommentsByAdId(1L)).hasSize(1);
  }

  @Test
  void getAuthorName_returnsName() {
    User u = new User();
    u.setName("Ivan");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(service.getAuthorName(1L)).isEqualTo("Ivan");
  }

  @Test
  void getAuthorName_unknownWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(service.getAuthorName(1L)).contains("Неизвестный");
  }

  @Test
  void getPhotoCount_returnsOneOrZero() {
    Announcement ad = new Announcement();
    ad.setPhoto(new byte[]{1});
    when(repository.findById(1L)).thenReturn(Optional.of(ad));
    assertThat(service.getPhotoCount(1L)).isEqualTo(1);

    ad.setPhoto(null);
    assertThat(service.getPhotoCount(1L)).isZero();
    ad.setPhoto(new byte[0]);
    assertThat(service.getPhotoCount(1L)).isZero();
  }

  @Test
  void getPhotoCount_zeroWhenAbsent() {
    when(repository.findById(99L)).thenReturn(Optional.empty());
    assertThat(service.getPhotoCount(99L)).isZero();
  }

  @Test
  void save_setsUpdatedAtAndPublishes() {
    Announcement ad = new Announcement();
    ad.setId(1L);
    ad.setStatus(AdStatus.ACTIVE);
    when(repository.save(ad)).thenReturn(ad);
    Announcement saved = service.save(ad);
    assertThat(saved.getUpdatedAt()).isNotNull();
    verify(kafkaMessageService).sendAnnouncementEvent(eq("announcement.updated"), eq("1"), any());
  }

  @Test
  void incrementViewCount_logsWhenZeroUpdates() {
    when(repository.incrementViews(1L)).thenReturn(1);
    service.incrementViewCount(1L);
    when(repository.incrementViews(2L)).thenReturn(0);
    service.incrementViewCount(2L);
  }
}
