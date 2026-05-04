package com.mipt.portal.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnumsTest {

  @Test
  void adStatus_displayNames() {
    for (AdStatus s : AdStatus.values()) {
      assertThat(s.getDisplayName()).isNotBlank();
    }
  }

  @Test
  void adStatus_predicates() {
    assertThat(AdStatus.ACTIVE.isActive()).isTrue();
    assertThat(AdStatus.DRAFT.isActive()).isFalse();
    assertThat(AdStatus.DELETED.isDeleted()).isTrue();
    assertThat(AdStatus.DRAFT.isDraft()).isTrue();
    assertThat(AdStatus.ACTIVE.canBeEdited()).isTrue();
    assertThat(AdStatus.DELETED.canBeEdited()).isFalse();
    assertThat(AdStatus.ARCHIVED.canBeEdited()).isFalse();
    assertThat(AdStatus.ACTIVE.isVisibleToPublic()).isTrue();
    assertThat(AdStatus.DRAFT.isVisibleToPublic()).isFalse();
    assertThat(AdStatus.ACTIVE.canBeArchived()).isTrue();
    assertThat(AdStatus.DRAFT.canBeArchived()).isFalse();
    assertThat(AdStatus.UNDER_MODERATION.isModerationRequired()).isTrue();
    assertThat(AdStatus.DRAFT.isModerationRequired()).isFalse();
  }

  @Test
  void category_displayNamesAndNumbers() {
    assertThat(Category.ELECTRONICS.getDisplayName()).isEqualTo("Электроника");
    assertThat(Category.ELECTRONICS.getNumber()).isEqualTo(1);
    assertThat(Category.OTHER.getNumber()).isEqualTo(21);
    for (Category c : Category.values()) {
      assertThat(c.getDisplayName()).isNotBlank();
    }
  }

  @Test
  void category_fromDisplayName() {
    assertThat(Category.fromDisplayName("Электроника")).isEqualTo(Category.ELECTRONICS);
    assertThat(Category.fromDisplayName("Книги и канцелярия")).isEqualTo(Category.BOOKS);
    assertThat(Category.fromDisplayName("Несуществует")).isEqualTo(Category.OTHER);
  }

  @Test
  void category_getByNumber() {
    assertThat(Category.getByNumber(1)).isEqualTo(Category.ELECTRONICS);
    assertThat(Category.getByNumber(21)).isEqualTo(Category.OTHER);
    assertThat(Category.getByNumber(0)).isEqualTo(Category.OTHER);
    assertThat(Category.getByNumber(999)).isEqualTo(Category.OTHER);
  }

  @Test
  void condition_displayNames() {
    assertThat(Condition.NEW.getDisplayName()).isEqualTo("Новое");
    assertThat(Condition.USED.getDisplayName()).isEqualTo("б/у");
    assertThat(Condition.BROKEN.getDisplayName()).isEqualTo("Не работает");
  }

  @Test
  void condition_displayConditions_doesNotThrow() {
    Condition.displayConditions();
  }

  @Test
  void condition_getByNumber() {
    assertThat(Condition.getByNumber(1)).isEqualTo(Condition.USED);
    assertThat(Condition.getByNumber(2)).isEqualTo(Condition.NEW);
    assertThat(Condition.getByNumber(0)).isEqualTo(Condition.USED);
    assertThat(Condition.getByNumber(999)).isEqualTo(Condition.USED);
  }

  @Test
  void role_authoritiesAndDisplayNames() {
    assertThat(Role.USER.getAuthority()).isEqualTo("ROLE_USER");
    assertThat(Role.MODERATOR.getAuthority()).isEqualTo("ROLE_MODERATOR");
    assertThat(Role.ADMIN.getAuthority()).isEqualTo("ROLE_ADMIN");
    assertThat(Role.USER.toString()).isEqualTo("Обычный пользователь");
    assertThat(Role.MODERATOR.getDisplayName()).isEqualTo("Модератор");
  }

  @Test
  void otherEnums_smoke() {
    assertThat(SanctionType.values()).contains(SanctionType.BAN, SanctionType.FREEZE);
    assertThat(AdminActionType.values()).contains(AdminActionType.ROLE_CHANGE);
    assertThat(AuditTargetType.values()).contains(AuditTargetType.USER);
  }
}
