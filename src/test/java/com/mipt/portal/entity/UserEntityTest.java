package com.mipt.portal.entity;

import com.mipt.portal.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

  @Test
  void newUser_hasDefaultRatingAndCoins() {
    User user = new User();
    assertThat(user.getRating()).isEqualTo(3.0);
    assertThat(user.getCoins()).isZero();
  }

  @Test
  void roles_addAndRemoveAndCheck() {
    User user = new User();
    assertThat(user.hasRole(Role.MODERATOR)).isFalse();
    user.addRole(Role.MODERATOR);
    assertThat(user.hasRole(Role.MODERATOR)).isTrue();
    assertThat(user.isModerator()).isTrue();
    assertThat(user.isAdmin()).isFalse();
    user.removeRole(Role.MODERATOR);
    assertThat(user.hasRole(Role.MODERATOR)).isFalse();
  }

  @Test
  void admin_isAlsoModerator() {
    User user = new User();
    user.addRole(Role.ADMIN);
    assertThat(user.isAdmin()).isTrue();
    assertThat(user.isModerator()).isTrue();
  }

  @Test
  void increaseRating_capsAt5() {
    User user = new User();
    user.setRating(4.5);
    user.increaseRating(1.0);
    assertThat(user.getRating()).isEqualTo(5.0);
  }

  @Test
  void increaseRating_normalCase() {
    User user = new User();
    user.setRating(3.0);
    user.increaseRating(0.5);
    assertThat(user.getRating()).isEqualTo(3.5);
  }

  @Test
  void decreaseRating_capsAt1() {
    User user = new User();
    user.setRating(1.5);
    user.decreaseRating(2.0);
    assertThat(user.getRating()).isEqualTo(1.0);
  }

  @Test
  void decreaseRating_normalCase() {
    User user = new User();
    user.setRating(4.0);
    user.decreaseRating(1.0);
    assertThat(user.getRating()).isEqualTo(3.0);
  }

  @Test
  void coins_addAndSpend() {
    User user = new User();
    user.addCoins(100);
    assertThat(user.getCoins()).isEqualTo(100);
    assertThat(user.spendCoins(40)).isTrue();
    assertThat(user.getCoins()).isEqualTo(60);
  }

  @Test
  void coins_spendFailsWhenInsufficient() {
    User user = new User();
    user.addCoins(10);
    assertThat(user.spendCoins(20)).isFalse();
    assertThat(user.getCoins()).isEqualTo(10);
  }

  @Test
  void isFrozen_falseWhenNullOrPast() {
    User user = new User();
    assertThat(user.isFrozen()).isFalse();
    user.setFrozenUntil(Instant.now().minusSeconds(10));
    assertThat(user.isFrozen()).isFalse();
  }

  @Test
  void isFrozen_trueWhenFuture() {
    User user = new User();
    user.setFrozenUntil(Instant.now().plusSeconds(60));
    assertThat(user.isFrozen()).isTrue();
  }

  @Test
  void isBanned_falseWhenNullOrPast() {
    User user = new User();
    assertThat(user.isBanned()).isFalse();
    user.setBannedUntil(Instant.now().minusSeconds(10));
    assertThat(user.isBanned()).isFalse();
  }

  @Test
  void isBanned_trueWhenFuture() {
    User user = new User();
    user.setBannedUntil(Instant.now().plusSeconds(60));
    assertThat(user.isBanned()).isTrue();
  }
}
