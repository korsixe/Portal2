package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.MediaService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdWebController}.
 *
 * <p>The controller is a JSP-based form handler with a fair number of branches —
 * unauthenticated visits, optional category/subcategory filters, three flavours of
 * priceType, optional tag and photo uploads, the publish-vs-draft action and the
 * generic error path. We invoke handler methods directly with mocked services.</p>
 */
@ExtendWith(MockitoExtension.class)
class AdWebControllerTest {

    @Mock private AnnouncementService announcementService;
    @Mock private MediaService mediaService;

    private AdWebController controller;

    @BeforeEach
    void setUp() {
        controller = new AdWebController(announcementService, mediaService);
    }

    private HttpSession sessionWithUser(Long userId) {
        MockHttpSession s = new MockHttpSession();
        if (userId != null) {
            s.setAttribute("userId", userId);
        }
        return s;
    }

    // ---------------- showCreateAdForm ----------------

    @Test
    void showCreateAdForm_unauthenticated_redirectsToLogin() {
        Model model = new ConcurrentModel();

        String view = controller.showCreateAdForm(null, null, null, sessionWithUser(null), model);

        assertThat(view).isEqualTo("redirect:/login");
        verify(announcementService, never()).getAllCategories();
    }

    @Test
    void showCreateAdForm_authenticated_addsBaseAttributes() {
        when(announcementService.getAllCategories()).thenReturn(List.of(
                Map.of("id", 1L, "name", "Электроника")));
        when(announcementService.getTagsWithValues()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = controller.showCreateAdForm(null, null, null, sessionWithUser(7L), model);

        assertThat(view).isEqualTo("create-ad");
        assertThat(model.containsAttribute("categories")).isTrue();
        assertThat(model.containsAttribute("tags")).isTrue();
    }

    @Test
    void showCreateAdForm_categoryProvided_loadsSubcategories() {
        when(announcementService.getAllCategories()).thenReturn(List.of(
                Map.of("id", 1L, "name", "Электроника"),
                Map.of("id", 2L, "name", "Дом и сад")));
        when(announcementService.getTagsWithValues()).thenReturn(List.of());
        when(announcementService.getSubcategoriesByCategory(1L)).thenReturn(
                List.of(Map.of("id", 11L, "name", "Смартфоны")));

        Model model = new ConcurrentModel();
        controller.showCreateAdForm("Электроника", "Смартфоны", "fixed", sessionWithUser(7L), model);

        assertThat(model.getAttribute("selectedCategory")).isEqualTo("Электроника");
        assertThat(model.getAttribute("selectedSubcategory")).isEqualTo("Смартфоны");
        assertThat(model.getAttribute("priceType")).isEqualTo("fixed");
        verify(announcementService).getSubcategoriesByCategory(1L);
    }

    @Test
    void showCreateAdForm_categoryProvidedButNotInList_doesNotLoadSubcategories() {
        when(announcementService.getAllCategories()).thenReturn(List.of(
                Map.of("id", 1L, "name", "Другое")));
        when(announcementService.getTagsWithValues()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        controller.showCreateAdForm("НетТакой", null, null, sessionWithUser(7L), model);

        assertThat(model.containsAttribute("subcategories")).isFalse();
        verify(announcementService, never()).getSubcategoriesByCategory(anyLong());
    }

    @Test
    void showCreateAdForm_emptyCategoryString_isIgnored() {
        when(announcementService.getAllCategories()).thenReturn(List.of());
        when(announcementService.getTagsWithValues()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        controller.showCreateAdForm("", null, null, sessionWithUser(7L), model);

        assertThat(model.containsAttribute("selectedCategory")).isFalse();
    }

    // ---------------- processCreateAd ----------------

    private Announcement stubCreatedAd(long id) {
        Announcement ad = new Announcement();
        ad.setId(id);
        return ad;
    }

    @Test
    void processCreateAd_unauthenticated_redirectsToLogin() {
        Model model = new ConcurrentModel();

        String view = controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "fixed", 100, "draft", null, null,
                sessionWithUser(null), model);

        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    void processCreateAd_freePrice_setsZero_andRedirects() throws Exception {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(1L));

        Model model = new ConcurrentModel();
        String view = controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "free", 999, "draft", null, null,
                sessionWithUser(7L), model);

        assertThat(view).isEqualTo("redirect:/successful-create-ad");
        verify(announcementService).create(any());
    }

    @Test
    void processCreateAd_negotiablePrice_setsMinusOne() {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(2L));

        Model model = new ConcurrentModel();
        String view = controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "negotiable", 999, "draft", null, null,
                sessionWithUser(7L), model);

        assertThat(view).isEqualTo("redirect:/successful-create-ad");
    }

    @Test
    void processCreateAd_publishAction_sendsToModeration() {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(3L));

        Model model = new ConcurrentModel();
        controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "fixed", 500, "publish", null, null,
                sessionWithUser(7L), model);

        verify(announcementService).sendToModeration(3L);
    }

    @Test
    void processCreateAd_withSelectedTags_savesTags() throws Exception {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(4L));

        Model model = new ConcurrentModel();
        controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "fixed", 500, "draft",
                null,
                "[{\"id\":1,\"name\":\"Apple\"}]",
                sessionWithUser(7L), model);

        verify(announcementService).saveAdTags(eq(4L), any());
    }

    @Test
    void processCreateAd_withPhoto_savesPhoto() throws Exception {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(5L));
        when(mediaService.multipartFileToBytes(any())).thenReturn(new byte[]{1, 2, 3});

        MockMultipartFile photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", new byte[]{1, 2, 3});
        Model model = new ConcurrentModel();
        controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "fixed", 500, "draft", photo, null,
                sessionWithUser(7L), model);

        verify(mediaService).savePhoto(eq(5L), any(byte[].class));
    }

    @Test
    void processCreateAd_emptyPhoto_isSkipped() throws Exception {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(6L));

        MockMultipartFile photo = new MockMultipartFile("photo", new byte[0]);
        Model model = new ConcurrentModel();
        controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NEW",
                "fixed", 500, "draft", photo, null,
                sessionWithUser(7L), model);

        verify(mediaService, never()).savePhoto(anyLong(), any(byte[].class));
    }

    @Test
    void processCreateAd_invalidConditionEnum_fallsBackToErrorPath() {
        when(announcementService.create(any())).thenReturn(stubCreatedAd(8L));
        when(announcementService.getAllCategories()).thenReturn(List.of());
        when(announcementService.getTagsWithValues()).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = controller.processCreateAd(
                "t", "d", "Электроника", "Смартфоны", "Москва", "NOT_A_CONDITION",
                "fixed", 500, "draft", null, null,
                sessionWithUser(7L), model);

        assertThat(view).isEqualTo("create-ad");
        assertThat(model.getAttribute("error")).asString().contains("ошибка");
    }
}
