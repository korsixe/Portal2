package com.mipt.portal.config;

import com.mipt.portal.enums.Role;
import com.mipt.portal.entity.User;
import com.mipt.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.mipt.portal.entity.Address;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import com.mipt.portal.repository.AnnouncementRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnnouncementRepository announcementRepository;

    @Override
    public void run(String... args) {
        User admin = createTestUser("admin.test@phystech.edu", "admin", Role.ADMIN);
        User moderator = createTestUser("moderator.test@phystech.edu", "moderator", Role.MODERATOR);
        User user = createTestUser("user.test@phystech.edu", "user", Role.USER);

        createSampleAnnouncements(admin, moderator, user);
    }

    private User createTestUser(String email, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            log.info("Creating user: {} with role: {}", email, role);
            User user = new User();
            user.setEmail(email);
            user.setName("Test " + role.name());
            
            String salt = UUID.randomUUID().toString().substring(0, 10);
            user.setSalt(salt);
            user.setHashPassword(passwordEncoder.encode(password + salt));
            
            user.addRole(role);
            user.setRating(3.0);
            user.setCoins(100);
            
            Address address = new Address();
            address.setFullAddress("Moscow, Kremlin");
            address.setCity("Moscow");
            address.setStreet("Kremlin");
            user.setAddress(address);
            
            userRepository.save(user);
            return user;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private void createSampleAnnouncements(User admin, User moderator, User regularUser) {
        if (announcementRepository.count() > 0) {
            return;
        }

        Long adminId = admin != null ? admin.getId() : null;
        Long moderatorId = moderator != null ? moderator.getId() : null;
        Long userId = regularUser != null ? regularUser.getId() : null;

        Announcement pending = new Announcement();
        pending.setTitle("MacBook Pro 14\" 2023");
        pending.setDescription("Состояние отличное, есть чек. Жду проверки модерации.");
        pending.setCategory(Category.ELECTRONICS);
        pending.setSubcategory("Ноутбуки");
        pending.setCondition(Condition.USED);
        pending.setPrice(145000);
        pending.setLocation("Москва, МФТИ");
        pending.setAuthorId(userId);
        pending.setStatus(AdStatus.UNDER_MODERATION);
        pending.setTags(List.of("macbook", "apple", "m1"));
        pending.setTagsCount(3);
        pending.setPhotoUrls(List.of("https://placehold.co/640x480"));

        Announcement active = new Announcement();
        active.setTitle("Смартфон Pixel 7a");
        active.setDescription("Официальная версия, полный комплект. Уже одобрено.");
        active.setCategory(Category.ELECTRONICS);
        active.setSubcategory("Смартфоны");
        active.setCondition(Condition.USED);
        active.setPrice(32000);
        active.setLocation("Долгопрудный");
        active.setAuthorId(moderatorId);
        active.setStatus(AdStatus.ACTIVE);
        active.setTags(List.of("google", "pixel"));
        active.setTagsCount(2);
        active.setPhotoUrls(List.of("https://placehold.co/600x400"));

        Announcement rejected = new Announcement();
        rejected.setTitle("Продам учебники");
        rejected.setDescription("Сборник задач по матанализу. Предыдущее объявление отклонено.");
        rejected.setCategory(Category.BOOKS);
        rejected.setSubcategory("Учебники");
        rejected.setCondition(Condition.USED);
        rejected.setPrice(1500);
        rejected.setLocation("Москва, ВДНХ");
        rejected.setAuthorId(adminId);
        rejected.setStatus(AdStatus.REJECTED);
        rejected.setTags(List.of("книги", "матан"));
        rejected.setTagsCount(2);
        rejected.setPhotoUrls(List.of("https://placehold.co/500x350"));

        announcementRepository.saveAll(List.of(pending, active, rejected));
    }
}
