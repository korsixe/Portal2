package com.mipt.portal.controller;

import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import com.mipt.portal.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdWebController {

  private final AnnouncementService announcementService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String LOGIN_REDIRECT = "redirect:/login.jsp";
  private static final String DASHBOARD_REDIRECT = "redirect:/dashboard.jsp";
  private static final String USER_ID_ATTR = "userId";
  private static final String ERROR_ATTR = "error";
  private static final String SUCCESS_ATTR = "success";
  private static final String ANNOUNCEMENT_ATTR = "announcement";

  @GetMapping("/create-ad")
  public String showCreateAdForm(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String subcategory,
    @RequestParam(required = false) String priceType,
    HttpSession session,
    Model model) {

    if (session.getAttribute(USER_ID_ATTR) == null) {
      return LOGIN_REDIRECT;
    }

    model.addAttribute("categories", announcementService.getAllCategories());
    model.addAttribute("tags", announcementService.getTagsWithValues());

    if (category != null && !category.isEmpty()) {
      model.addAttribute("selectedCategory", category);
      List<Map<String, Object>> categories = announcementService.getAllCategories();
      Long categoryId = null;
      for (Map<String, Object> cat : categories) {
        if (cat.get("name").equals(category)) {
          categoryId = ((Number) cat.get("id")).longValue();
          break;
        }
      }
      if (categoryId != null) {
        model.addAttribute("subcategories", announcementService.getSubcategoriesByCategory(categoryId));
      }
    }

    if (subcategory != null) {
      model.addAttribute("selectedSubcategory", subcategory);
    }

    if (priceType != null) {
      model.addAttribute("priceType", priceType);
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
    @RequestParam(value = "selectedTags", required = false) String selectedTags,
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

      if (selectedTags != null && !selectedTags.isEmpty()) {
        List<Map<String, Object>> tagList = objectMapper.readValue(selectedTags,
          objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        announcementService.saveAdTags(ad.getId(), tagList);
      }

      if ("publish".equals(action)) {
        announcementService.sendToModeration(ad.getId());
      }

      model.addAttribute(SUCCESS_ATTR, "Объявление успешно создано!");
      return DASHBOARD_REDIRECT;

    } catch (Exception e) {
      model.addAttribute(ERROR_ATTR, "Произошла ошибка при создании: " + e.getMessage());
      model.addAttribute("categories", announcementService.getAllCategories());
      model.addAttribute("tags", announcementService.getTagsWithValues());
      model.addAttribute("title", title);
      model.addAttribute("description", description);
      model.addAttribute("selectedCategory", categoryName);
      model.addAttribute("selectedSubcategory", subcategory);
      model.addAttribute("location", location);
      model.addAttribute("condition", conditionName);
      model.addAttribute("priceType", priceType);
      model.addAttribute("price", price);
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
    model.addAttribute("categories", announcementService.getAllCategories());
    model.addAttribute("tags", announcementService.getTagsWithValues());
    model.addAttribute("adTags", announcementService.getTagsForAd(adId));

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
    @RequestParam(value = "selectedTags", required = false) String selectedTags,
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

      if (selectedTags != null && !selectedTags.isEmpty()) {
        List<Map<String, Object>> tagList = objectMapper.readValue(selectedTags,
          objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        announcementService.saveAdTags(ad.getId(), tagList);
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
      model.addAttribute("categories", announcementService.getAllCategories());
      model.addAttribute("tags", announcementService.getTagsWithValues());
      return "edit-ad";
    }
  }
}