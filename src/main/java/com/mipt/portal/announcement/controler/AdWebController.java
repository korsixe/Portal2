package com.mipt.portal.announcement.controler;

import com.mipt.portal.announcement.dto.AnnouncementCreateDto;
import com.mipt.portal.announcement.entity.Announcement;
import com.mipt.portal.announcement.enums.Category;
import com.mipt.portal.announcement.enums.Condition;
import com.mipt.portal.announcement.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequiredArgsConstructor
public class AdWebController {

  private final AnnouncementService announcementService;
  private static final String LOGIN_REDIRECT = "redirect:/login.jsp";
  private static final String DASHBOARD_REDIRECT = "redirect:/dashboard.jsp";
  private static final String USER_ID_ATTR = "userId";
  private static final String ERROR_ATTR = "error";
  private static final String SUCCESS_ATTR = "success";
  private static final String ANNOUNCEMENT_ATTR = "announcement";

  @GetMapping("/create-ad")
  public String showCreateAdForm(HttpSession session) {
    if (session.getAttribute(USER_ID_ATTR) == null) {
      return LOGIN_REDIRECT;
    }
    return "create-ad";
  }

  @PostMapping("/create-ad")
  public String processCreateAd(
          @RequestParam("title") String title,
          @RequestParam("description") String description,
          @RequestParam("category") String categoryName,
          @RequestParam("subcategory") String subcategory,
          @RequestParam("location") String location,
          @RequestParam("condition") String conditionName,
          @RequestParam("priceType") String priceType,
          @RequestParam(value = "price", required = false, defaultValue = "0") int price,
          @RequestParam("action") String action,
          @RequestParam(value = "photos", required = false) MultipartFile[] photos,
          HttpSession session,
          Model model) {

    Long authorId = (Long) session.getAttribute(USER_ID_ATTR);
    if (authorId == null) {
      return LOGIN_REDIRECT;
    }

    try {
      AnnouncementCreateDto dto = new AnnouncementCreateDto();
      dto.setTitle(title);
      dto.setDescription(description);
      dto.setAuthorId(authorId);

      if ("free".equals(priceType)) {
        dto.setPrice(0);
      } else if ("negotiable".equals(priceType)) {
        dto.setPrice(-1);
      } else {
        dto.setPrice(price);
      }

      Announcement ad = announcementService.create(dto);

      ad.setCategory(Category.fromDisplayName(categoryName));
      ad.setSubcategory(subcategory);
      ad.setLocation(location);
      ad.setCondition(Condition.valueOf(conditionName));

      if ("publish".equals(action)) {
        announcementService.sendToModeration(ad.getId());
      }


      model.addAttribute(SUCCESS_ATTR, "Объявление успешно создано!");
      return DASHBOARD_REDIRECT;

    } catch (Exception e) {
      model.addAttribute(ERROR_ATTR, "Произошла ошибка при создании: " + e.getMessage());
      return "create-ad";
    }
  }

  @GetMapping("/edit-ad")
  public String showEditAdForm(@RequestParam("adId") Long adId, HttpSession session, Model model) {
    Long currentUserId = (Long) session.getAttribute(USER_ID_ATTR);
    if (currentUserId == null) {
      return LOGIN_REDIRECT;
    }

    Announcement ad = announcementService.findById(adId);
    if (ad == null) {
      model.addAttribute(ERROR_ATTR, "Объявление не найдено!");
      return DASHBOARD_REDIRECT;
    }

    if (!ad.getAuthorId().equals(currentUserId)) {
      model.addAttribute(ERROR_ATTR, "У вас нет прав редактировать чужое объявление!");
      return DASHBOARD_REDIRECT;
    }

    model.addAttribute(ANNOUNCEMENT_ATTR, ad);

    return "edit-ad";
  }

  @PostMapping("/edit-ad")
  public String processEditAd(
          @RequestParam("adId") Long adId,
          @RequestParam("title") String title,
          @RequestParam("description") String description,
          @RequestParam("category") String categoryName,
          @RequestParam("subcategory") String subcategory,
          @RequestParam("location") String location,
          @RequestParam("condition") String conditionName,
          @RequestParam("priceType") String priceType,
          @RequestParam(value = "price", required = false, defaultValue = "0") int price,
          @RequestParam("action") String action,
          @RequestParam(value = "photos", required = false) MultipartFile[] photos,
          HttpSession session,
          Model model) {

    Long currentUserId = (Long) session.getAttribute(USER_ID_ATTR);
    if (currentUserId == null) {
      return LOGIN_REDIRECT;
    }

    try {
      Announcement ad = announcementService.findById(adId);
      if (ad == null || !ad.getAuthorId().equals(currentUserId)) {
        return DASHBOARD_REDIRECT;
      }

      ad.setTitle(title);
      ad.setDescription(description);
      ad.setCategory(Category.fromDisplayName(categoryName));
      ad.setSubcategory(subcategory);
      ad.setLocation(location);
      ad.setCondition(Condition.valueOf(conditionName));

      if ("free".equals(priceType)) {
        ad.setPrice(0);
      } else if ("negotiable".equals(priceType)) {
        ad.setPrice(-1);
      } else {
        ad.setPrice(price);
      }

      if ("publish".equals(action)) {
        announcementService.sendToModeration(ad.getId());
      }

      announcementService.save(ad);

      model.addAttribute(SUCCESS_ATTR, "Объявление успешно обновлено!");
      return DASHBOARD_REDIRECT;

    } catch (Exception e) {
      model.addAttribute(ERROR_ATTR, "Ошибка при обновлении: " + e.getMessage());
      Announcement ad = announcementService.findById(adId);
      model.addAttribute(ANNOUNCEMENT_ATTR, ad);
      return "edit-ad";
    }
  }
}