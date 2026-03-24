package com.mipt.portal.enums;

/**
 * Перечисление статусов объявления.
 */
public enum AdStatus {
  DRAFT("Черновик"),
  UNDER_MODERATION("На модерации"),
  ACTIVE("Активно"),
  REJECTED("Отклонено"),   // Добавили статус отклонения модератором
  ARCHIVED("Архив"),
  DELETED("Удалено");      // Полезно для "мягкого" удаления (soft-delete)

  private final String displayName;

  AdStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isActive() {
    return this == ACTIVE;
  }

  public boolean isDeleted() {
    return this == DELETED;
  }

  public boolean isDraft() {
    return this == DRAFT;
  }

  public boolean canBeEdited() {
    return this != DELETED && this != ARCHIVED;
  }

  public boolean isVisibleToPublic() {
    return this == ACTIVE;
  }

  public boolean canBeArchived() {
    return this == ACTIVE;
  }

  public boolean isModerationRequired() {
    return this == UNDER_MODERATION;
  }
}
