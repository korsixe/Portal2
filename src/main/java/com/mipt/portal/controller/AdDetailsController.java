package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Comment;
import com.mipt.portal.entity.User;
import com.mipt.portal.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdDetailsController {

  private final AnnouncementService announcementService;

  @GetMapping("/ad-details")
  public String showAdDetails(@RequestParam("id") Long adId, Model model) {
    Announcement announcement = announcementService.findById(adId);
    if (announcement == null) {
      model.addAttribute("error", "Объявление не найдено");
      return "redirect:/home";
    }

    String authorName = announcementService.getAuthorName(announcement.getAuthorId());
    int photoCount = announcementService.getPhotoCount(adId);
    List<Comment> comments = announcementService.getCommentsByAdId(adId);

    model.addAttribute("announcement", announcement);
    model.addAttribute("authorName", authorName);
    model.addAttribute("photoCount", photoCount);
    model.addAttribute("comments", comments);

    return "ad-details";
  }

  @PostMapping("/ad-details")
  public String addComment(
    @RequestParam("id") Long adId,
    @RequestParam("commentText") String commentText,
    HttpSession session,
    Model model) {

    log.info("=== ADD COMMENT ===");
    log.info("adId: {}", adId);
    log.info("commentText: {}", commentText);

    User user = (User) session.getAttribute("user");
    log.info("user: {}", user != null ? user.getId() : "null");

    if (user == null) {
      model.addAttribute("error", "Для добавления комментария необходимо авторизоваться");
      return "redirect:/login";
    }

    if (commentText == null || commentText.trim().isEmpty()) {
      model.addAttribute("error", "Комментарий не может быть пустым");
      return "redirect:/ad-details?id=" + adId;
    }

    try {
      announcementService.addComment(adId, user.getId(), user.getName(), commentText.trim());
      log.info("Comment added successfully");
    } catch (Exception e) {
      log.error("Error adding comment", e);
      model.addAttribute("error", "Ошибка при сохранении комментария");
    }

    return "redirect:/ad-details?id=" + adId;
  }
}