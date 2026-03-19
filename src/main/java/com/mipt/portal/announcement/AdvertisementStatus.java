package com.mipt.portal.announcement;

public enum AdvertisementStatus {
  DRAFT("Черновик"),
  UNDER_MODERATION("На модерации"),
  ACTIVE("Активно"),
  ARCHIVED("Архив"),
  DELETED("Удалено");

  private final String displayName;

  AdvertisementStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static void displayStatuses() {
    System.out.println("Статусы объявлений:");
    for (int i = 0; i < values().length; i++) {
      System.out.println((i + 1) + ". " + values()[i].getDisplayName());
    }
  }

  public static AdvertisementStatus getByNumber(int number) {
    if (number > 0 && number <= values().length) {
      return values()[number - 1];
    }
    throw new IllegalArgumentException("Неверный номер статуса: " + number);
  }

  public static AdvertisementStatus getByDisplayName(String displayName) {
    for (AdvertisementStatus status : values()) {
      if (status.getDisplayName().equalsIgnoreCase(displayName)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Неизвестный статус: " + displayName);
  }

  // Бизнес-логика
  public boolean isActive() {
    return this == ACTIVE;
  }

  public boolean isDelete() {
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

  public boolean canBePublished() {
    return this == DRAFT || this == ARCHIVED;
  }

  public boolean canBeArchived() {
    return this == ACTIVE;
  }

  public boolean isModerationRequired() {
    return this == UNDER_MODERATION;
  }

  public static AdvertisementStatus[] getEditableStatuses() {
    return new AdvertisementStatus[]{DRAFT, UNDER_MODERATION, ACTIVE, ARCHIVED};
  }

  public static AdvertisementStatus[] getPublicStatuses() {
    return new AdvertisementStatus[]{ACTIVE};
  }

  public static AdvertisementStatus[] getUserVisibleStatuses() {
    return new AdvertisementStatus[]{DRAFT, UNDER_MODERATION, ACTIVE, ARCHIVED};
  }
}