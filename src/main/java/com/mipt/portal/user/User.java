package com.mipt.portal.user;

import com.mipt.portal.address.Address;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*; // для бд

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")  /// TODO:: добавить аннотации с колонками для бд
public class User {

  @Id
  private Long id;
  private String email;
  private String hashPassword; // хэш пароля
  private String salt; // соль
  private String name;
  @Embedded
  private Address address;
  private String studyProgram;
  private int course;
  private double rating;
  private int coins;
  private List<Long> adList;
  private Set<Role> roles = new HashSet<>(); // роли пользователя

  /**
   * Проверяет, есть ли у пользователя определенная роль
   */
  public boolean hasRole(Role role) {
    return roles.contains(role);
  }

  /**
   * Проверяет, является ли пользователь модератором
   */
  public boolean isModerator() {
    return hasRole(Role.MODERATOR) || hasRole(Role.ADMIN);
  }

  /**
   * Добавляет роль пользователю
   */
  public void addRole(Role role) {
    this.roles.add(role);
  }

  /**
   * Удаляет роль у пользователя
   */
  public void removeRole(Role role) {
    this.roles.remove(role);
  }

  public void increaseRating(double increment) {
    if (this.rating + increment <= 5) {
      this.rating += increment;
    } else {
      this.rating = 5;
    }
  }

  public void decreaseRating(double decrement) {
    if (this.rating - decrement >= 1) {
      this.rating -= decrement;
    } else {
      this.rating = 1;
    }
  }

  public void addCoins(int amount) {
    this.coins += amount;
  }

  public boolean spendCoins(int amount) {
    if (this.coins - amount < 0) {
      return false;
    }
    this.coins -= amount;
    return true;
  }
}