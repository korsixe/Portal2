package com.mipt.portal.dto;

import com.mipt.portal.entity.AdminActionAudit;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSmokeTest {

  @Test
  void adminDashboardResponse() {
    SystemStats s = new SystemStats(10, 1, 2, 7);
    AdminDashboardResponse r = new AdminDashboardResponse(List.of(new User()), s);
    assertThat(r.getUsers()).hasSize(1);
    assertThat(r.getStats()).isSameAs(s);
  }

  @Test
  void adminResponse_allConstructorsAndAccessors() {
    AdminResponse r = new AdminResponse();
    r.setSuccess(true);
    r.setMessage("ok");
    r.setData("data");
    assertThat(r.isSuccess()).isTrue();
    assertThat(r.getMessage()).isEqualTo("ok");
    assertThat(r.getData()).isEqualTo("data");

    AdminResponse all = new AdminResponse(true, "msg", 42);
    assertThat(all.getData()).isEqualTo(42);

    AdminResponse two = new AdminResponse(false, "fail");
    assertThat(two.getData()).isNull();

    AdminResponse another = new AdminResponse(true, "msg", 42);
    assertThat(all).isEqualTo(another).hasSameHashCodeAs(another);
    assertThat(all.toString()).contains("msg");
  }

  @Test
  void announcementCreateDto() {
    AnnouncementCreateDto d = new AnnouncementCreateDto();
    d.setTitle("t");
    d.setDescription("desc");
    d.setCategory("ELECTRONICS");
    d.setSubcategory("phones");
    d.setLocation("Moscow");
    d.setCondition("USED");
    d.setPrice(100);
    d.setAuthorId(1L);
    d.setPhotoUrls(List.of("u"));
    assertThat(d.getTitle()).isEqualTo("t");
    assertThat(d.getPrice()).isEqualTo(100);
    assertThat(d.getPhotoUrls()).containsExactly("u");
    assertThat(d.toString()).contains("t");
  }

  @Test
  void announcementFilterDto() {
    AnnouncementFilterDto f = new AnnouncementFilterDto();
    f.setText("text");
    f.setSortBy("price");
    f.setSortDirection("ASC");
    f.setMinPrice(10);
    f.setMaxPrice(500);
    f.setCategory(Category.BOOKS);
    f.setSubcategory("sub");
    f.setCondition(Condition.NEW);
    f.setCreatedAfter(Instant.now());
    assertThat(f.getMinPrice()).isEqualTo(10);
    assertThat(f.getCategory()).isEqualTo(Category.BOOKS);
    assertThat(f.toString()).contains("price");
  }

  @Test
  void announcementUpdateDto() {
    AnnouncementUpdateDto d = new AnnouncementUpdateDto();
    d.setTitle("t");
    d.setDescription("d");
    d.setCategory("c");
    d.setSubcategory("s");
    d.setLocation("l");
    d.setCondition("USED");
    d.setAction("publish");
    d.setPrice(10);
    assertThat(d.getAction()).isEqualTo("publish");
  }

  @Test
  void changePasswordRequest_setterGetter() {
    ChangePasswordRequest r = new ChangePasswordRequest();
    r.setCurrentPassword("old");
    r.setNewPassword("new");
    r.setConfirmPassword("new");
    assertThat(r.getCurrentPassword()).isEqualTo("old");
    assertThat(r.getNewPassword()).isEqualTo("new");
    assertThat(r.getConfirmPassword()).isEqualTo("new");
  }

  @Test
  void coinManagementRequest() {
    CoinManagementRequest r = new CoinManagementRequest(1L, 100, "add", "bonus");
    assertThat(r.getTargetUserId()).isEqualTo(1L);
    assertThat(r.getAmount()).isEqualTo(100);
    assertThat(r.getAction()).isEqualTo("add");
    assertThat(r.getReason()).isEqualTo("bonus");
    CoinManagementRequest empty = new CoinManagementRequest();
    empty.setAmount(5);
    assertThat(empty.getAmount()).isEqualTo(5);
  }

  @Test
  void commentDtos() {
    CommentCreateDto c = new CommentCreateDto();
    c.setContent("hi");
    assertThat(c.getContent()).isEqualTo("hi");

    CommentDTO dto = CommentDTO.builder()
        .id(1L).advertisementId(2L).userId(3L)
        .author("Ivan").text("comment")
        .createdAt(LocalDateTime.now()).build();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getAuthor()).isEqualTo("Ivan");
    CommentDTO dto2 = new CommentDTO();
    dto2.setId(1L);
    dto2.setText("comment");
    assertThat(dto2.getId()).isEqualTo(1L);
  }

  @Test
  void deleteAccountRequest() {
    DeleteAccountRequest r = new DeleteAccountRequest();
    r.setPassword("p");
    assertThat(r.getPassword()).isEqualTo("p");
  }

  @Test
  void errorResponse() {
    ErrorResponse e = new ErrorResponse("bad", "field", 400);
    assertThat(e.getMessage()).isEqualTo("bad");
    ErrorResponse b = ErrorResponse.builder().message("m").field("f").status(500).build();
    assertThat(b.getStatus()).isEqualTo(500);
    ErrorResponse empty = new ErrorResponse();
    empty.setStatus(200);
    assertThat(empty.getStatus()).isEqualTo(200);
  }

  @Test
  void loginRequest() {
    LoginRequest r = new LoginRequest();
    r.setEmail("e@phystech.edu");
    r.setPassword("p");
    assertThat(r.getEmail()).isEqualTo("e@phystech.edu");
  }

  @Test
  void moderationActionRequest() {
    ModerationActionRequest r = new ModerationActionRequest();
    r.setAdId(1L);
    r.setReason("r");
    assertThat(r.getAdId()).isEqualTo(1L);
    assertThat(r.getReason()).isEqualTo("r");
  }

  @Test
  void moderationHistoryResponse() {
    ModerationHistoryResponse r = new ModerationHistoryResponse(
        List.of(new ModerationHistory()), List.of(new AdminActionAudit()));
    assertThat(r.getHistory()).hasSize(1);
    assertThat(r.getAdminActions()).hasSize(1);
  }

  @Test
  void moderatorDashboardResponse() {
    ModeratorDashboardResponse r = new ModeratorDashboardResponse(
        List.of(new Announcement()), new SystemStats(1, 1, 0, 0), new User());
    assertThat(r.getAds()).hasSize(1);
    assertThat(r.getModerator()).isNotNull();
    assertThat(r.getStats().getTotalUsers()).isEqualTo(1);
  }

  @Test
  void profanityRequestResponse() {
    ProfanityCheckRequest req = new ProfanityCheckRequest();
    req.setText("hello");
    assertThat(req.getText()).isEqualTo("hello");

    ProfanityCheckResponse resp = new ProfanityCheckResponse(true);
    assertThat(resp.isHasProfanity()).isTrue();
    ProfanityCheckResponse empty = new ProfanityCheckResponse();
    empty.setHasProfanity(false);
    assertThat(empty.isHasProfanity()).isFalse();
  }

  @Test
  void registerRequest() {
    RegisterRequest r = new RegisterRequest();
    r.setEmail("e@phystech.edu");
    r.setName("Ivan");
    r.setPassword("p");
    r.setPasswordAgain("p");
    r.setAddress("addr");
    r.setStudyProgram("PM");
    r.setCourse(3);
    assertThat(r.getCourse()).isEqualTo(3);
    assertThat(r.getEmail()).isEqualTo("e@phystech.edu");
  }

  @Test
  void roleManagementRequest() {
    RoleManagementRequest r = new RoleManagementRequest(1L, "promote", "MODERATOR", "good");
    assertThat(r.getRole()).isEqualTo("MODERATOR");
    RoleManagementRequest empty = new RoleManagementRequest();
    empty.setReason("r");
    assertThat(empty.getReason()).isEqualTo("r");
  }

  @Test
  void sanctionRequest() {
    SanctionRequest s = new SanctionRequest();
    s.setTargetUserId(1L);
    s.setReason("spam");
    s.setDuration(5);
    s.setType("ban");
    assertThat(s.getDuration()).isEqualTo(5);
    assertThat(s.getType()).isEqualTo("ban");
  }

  @Test
  void simpleActionResponse() {
    SimpleActionResponse r = new SimpleActionResponse(true, "ok");
    assertThat(r.isSuccess()).isTrue();
    assertThat(r.getMessage()).isEqualTo("ok");
  }

  @Test
  void supportRequestCreateDto() {
    SupportRequestCreateDto s = new SupportRequestCreateDto();
    s.setMessage("help");
    assertThat(s.getMessage()).isEqualTo("help");
  }

  @Test
  void systemStats() {
    SystemStats s = new SystemStats(10, 1, 2, 7);
    assertThat(s.getTotalUsers()).isEqualTo(10);
    assertThat(s.getAdminCount()).isEqualTo(1);
    assertThat(s.getModeratorCount()).isEqualTo(2);
    assertThat(s.getRegularUserCount()).isEqualTo(7);
    assertThat(s.toString()).contains("10");
  }

  @Test
  void userUpdateRequest() {
    UserUpdateRequest r = new UserUpdateRequest();
    r.setName("Ivan");
    r.setStudyProgram("PM");
    r.setCourse(3);
    UserUpdateRequest.AddressDTO a = new UserUpdateRequest.AddressDTO();
    a.setFullAddress("Москва");
    a.setCity("Moscow");
    a.setStreet("Tverskaya");
    a.setHouseNumber("1");
    a.setBuilding("A");
    r.setAddress(a);
    assertThat(r.getName()).isEqualTo("Ivan");
    assertThat(r.getAddress().getCity()).isEqualTo("Moscow");
  }
}
