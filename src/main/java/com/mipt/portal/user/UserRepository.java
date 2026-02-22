package com.mipt.portal.user;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {

  Optional<User> save(User user);

  Optional<User> findByEmail(String email);

  Optional<User> findById(long id);

  boolean update(User user);

  boolean delete(long id);

  boolean existsByEmail(String email);

  List<User> findAll();

  default boolean existsById(long id) {
    return findById(id).isPresent();
  }

  default void deleteById(long id) {
    delete(id);
  }
}