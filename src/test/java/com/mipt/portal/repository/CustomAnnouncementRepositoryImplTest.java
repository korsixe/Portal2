package com.mipt.portal.repository;

import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomAnnouncementRepositoryImpl}.
 *
 * <p>The implementation builds a JPQL string from a filter DTO and binds parameters.
 * We mock {@link EntityManager} + {@link TypedQuery} and check both the produced JPQL
 * and the set of bound parameters across all branch combinations.</p>
 */
class CustomAnnouncementRepositoryImplTest {

    private EntityManager em;
    @SuppressWarnings("unchecked")
    private TypedQuery<Announcement> query;
    private CustomAnnouncementRepositoryImpl repo;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        em = mock(EntityManager.class);
        query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());
        when(em.createQuery(anyString(), eq(Announcement.class))).thenReturn(query);
        repo = new CustomAnnouncementRepositoryImpl();
        ReflectionTestUtils.setField(repo, "em", em);
    }

    private String capturedJpql() {
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(jpql.capture(), eq(Announcement.class));
        return jpql.getValue();
    }

    @Test
    void searchApproved_emptyFilter_defaultSortByCreatedAtDesc() {
        repo.searchApproved(new AnnouncementFilterDto(), null, null);

        String jpql = capturedJpql();
        assertThat(jpql).contains("a.status = :status")
                .contains("ORDER BY a.createdAt DESC")
                .doesNotContain("LOWER(a.title)")
                .doesNotContain(":minPrice");
        verify(query).setParameter("status", com.mipt.portal.enums.AdStatus.ACTIVE);
    }

    @Test
    void searchApproved_textFilter_addsLowerLikeClause() {
        AnnouncementFilterDto f = new AnnouncementFilterDto();
        f.setText("iPhone");
        repo.searchApproved(f, null, null);

        assertThat(capturedJpql()).contains("LOWER(a.title) LIKE LOWER(:text)");
        verify(query).setParameter("text", "%iPhone%");
    }

    @Test
    void searchApproved_blankText_isIgnored() {
        AnnouncementFilterDto f = new AnnouncementFilterDto();
        f.setText("   ");
        repo.searchApproved(f, null, null);

        assertThat(capturedJpql()).doesNotContain(":text");
    }

    @Test
    void searchApproved_priceRange_bindsBothParams() {
        AnnouncementFilterDto f = new AnnouncementFilterDto();
        f.setMinPrice(100);
        f.setMaxPrice(500);
        repo.searchApproved(f, null, null);

        assertThat(capturedJpql()).contains(":minPrice").contains(":maxPrice");
        verify(query).setParameter("minPrice", 100);
        verify(query).setParameter("maxPrice", 500);
    }

    @Test
    void searchApproved_categorySubcategoryConditionCreatedAfter() {
        AnnouncementFilterDto f = new AnnouncementFilterDto();
        f.setCategory(Category.ELECTRONICS);
        f.setSubcategory("Смартфоны");
        f.setCondition(Condition.NEW);
        Instant cutoff = Instant.parse("2024-01-01T00:00:00Z");
        f.setCreatedAfter(cutoff);

        repo.searchApproved(f, null, null);

        String jpql = capturedJpql();
        assertThat(jpql).contains(":category").contains(":subcategory")
                .contains(":condition").contains(":createdAfter");
        verify(query).setParameter("category", Category.ELECTRONICS);
        verify(query).setParameter("subcategory", "Смартфоны");
        verify(query).setParameter("condition", Condition.NEW);
        verify(query).setParameter("createdAfter", cutoff);
    }

    @Test
    void searchApproved_blankSubcategory_isIgnored() {
        AnnouncementFilterDto f = new AnnouncementFilterDto();
        f.setSubcategory("");
        repo.searchApproved(f, null, null);

        assertThat(capturedJpql()).doesNotContain(":subcategory");
    }

    @Test
    void searchApproved_explicitAscDirection_buildsAscOrderBy() {
        repo.searchApproved(new AnnouncementFilterDto(), "price", "ASC");

        assertThat(capturedJpql()).contains("ORDER BY a.price ASC");
    }

    @Test
    void searchApproved_invalidSortField_omitsOrderBy() {
        repo.searchApproved(new AnnouncementFilterDto(), "drop table users; --", null);

        assertThat(capturedJpql()).doesNotContain("ORDER BY");
    }

    @Test
    void searchApproved_blankSortBy_fallsBackToCreatedAt() {
        repo.searchApproved(new AnnouncementFilterDto(), "   ", "ASC");

        assertThat(capturedJpql()).contains("ORDER BY a.createdAt ASC");
    }

    @Test
    void searchApproved_setsStatusParamExactlyOnce() {
        repo.searchApproved(new AnnouncementFilterDto(), null, null);
        verify(query, times(1)).setParameter("status", com.mipt.portal.enums.AdStatus.ACTIVE);
    }
}
