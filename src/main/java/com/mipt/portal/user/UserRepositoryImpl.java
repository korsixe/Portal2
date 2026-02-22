package com.mipt.portal.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private final ConcurrentHashMap<Long, User> userStorage = new ConcurrentHashMap<>(); // пока храним в Мапе, вместо бд
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public Optional<User> save(User user) {
    if (user.getId() == null) {
      user.setId(idGenerator.getAndIncrement());
      userStorage.put(user.getId(), user);
      return Optional.of(user);
    }

    if (userStorage.containsKey(user.getId())) {
      userStorage.put(user.getId(), user);
      return Optional.of(user);
    }

    return Optional.empty();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userStorage.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
  }

  @Override
  public Optional<User> findById(long id) {
    return Optional.ofNullable(userStorage.get(id));
  }

  @Override
  public boolean existsByEmail(String email) {
    return userStorage.values().stream().anyMatch(user -> user.getEmail().equals(email));
  }

  @Override
  public List<User> findAll() {
    return new ArrayList<>(userStorage.values());
  }

  @Override
  public boolean delete(long id) {
    return userStorage.remove(id) != null;
  }

  @Override
  public boolean update(User user) {
    if (user.getId() == null) {
      return false;
    }
    if (!userStorage.containsKey(user.getId())) {
      return false;
    }
    userStorage.put(user.getId(), user);
    return true;
  }
}