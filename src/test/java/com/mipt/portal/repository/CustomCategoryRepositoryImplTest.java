package com.mipt.portal.repository;

import com.mipt.portal.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomCategoryRepositoryImpl}.
 *
 * <p>The implementation is a thin layer over {@link EntityManager}. We mock both EM and
 * the produced TypedQuery so each method's branches (empty/non-empty result, parent
 * present/absent, is_service true/false) can be exercised in isolation.</p>
 */
class CustomCategoryRepositoryImplTest {

    private EntityManager em;
    private CustomCategoryRepositoryImpl repo;

    private Category cat(Long id, String name, Boolean isService, Category parent) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setIsService(isService);
        c.setParent(parent);
        return c;
    }

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        repo = new CustomCategoryRepositoryImpl();
        ReflectionTestUtils.setField(repo, "em", em);
    }

    @SuppressWarnings("unchecked")
    private <T> TypedQuery<T> stubTypedQuery(List<T> result) {
        TypedQuery<T> q = mock(TypedQuery.class);
        when(q.getResultList()).thenReturn(result);
        when(q.setParameter(anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(q);
        when(q.setMaxResults(org.mockito.ArgumentMatchers.anyInt())).thenReturn(q);
        return q;
    }

    @Test
    void getAllCategories_returnsLinkedMapsWithIdNameAndIsServiceFlag() {
        Category root1 = cat(1L, "Электроника", false, null);
        Category root2 = cat(2L, "Услуги", true, null);
        TypedQuery<Category> q = stubTypedQuery(List.of(root1, root2));
        when(em.createQuery(anyString(), eq(Category.class))).thenReturn(q);

        List<Map<String, Object>> result = repo.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("id", 1L)
                .containsEntry("name", "Электроника").containsEntry("isService", false);
        assertThat(result.get(1)).containsEntry("isService", true);
    }

    @Test
    void getAllCategories_isServiceNull_treatedAsFalse() {
        Category root = cat(1L, "Без флага", null, null);
        TypedQuery<Category> q = stubTypedQuery(List.of(root));
        when(em.createQuery(anyString(), eq(Category.class))).thenReturn(q);

        List<Map<String, Object>> result = repo.getAllCategories();
        assertThat(result.get(0)).containsEntry("isService", false);
    }

    @Test
    void getSubcategoriesByCategory_returnsOnlyIdAndName() {
        Category sub1 = cat(11L, "Смартфоны", false, cat(1L, "Электроника", false, null));
        TypedQuery<Category> q = stubTypedQuery(List.of(sub1));
        when(em.createQuery(anyString(), eq(Category.class))).thenReturn(q);

        List<Map<String, Object>> result = repo.getSubcategoriesByCategory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsOnlyKeys("id", "name")
                .containsEntry("id", 11L).containsEntry("name", "Смартфоны");
    }

    @Test
    void isServiceSubcategory_trueWhenParentIsService() {
        TypedQuery<Boolean> q = stubTypedQuery(List.of(Boolean.TRUE));
        when(em.createQuery(anyString(), eq(Boolean.class))).thenReturn(q);

        assertThat(repo.isServiceSubcategory(11L)).isTrue();
    }

    @Test
    void isServiceSubcategory_falseWhenParentIsNotService() {
        TypedQuery<Boolean> q = stubTypedQuery(List.of(Boolean.FALSE));
        when(em.createQuery(anyString(), eq(Boolean.class))).thenReturn(q);

        assertThat(repo.isServiceSubcategory(11L)).isFalse();
    }

    @Test
    void isServiceSubcategory_falseWhenNoResult() {
        TypedQuery<Boolean> q = stubTypedQuery(List.of());
        when(em.createQuery(anyString(), eq(Boolean.class))).thenReturn(q);

        assertThat(repo.isServiceSubcategory(99L)).isFalse();
    }

    @Test
    void getParentCategoryIdByName_returnsId_orNullIfMissing() {
        TypedQuery<Long> okQuery = stubTypedQuery(List.of(7L));
        TypedQuery<Long> emptyQuery = stubTypedQuery(List.of());
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(okQuery, emptyQuery);

        assertThat(repo.getParentCategoryIdByName("Смартфоны")).isEqualTo(7L);
        assertThat(repo.getParentCategoryIdByName("Несуществующее")).isNull();
    }

    @Test
    void getSubcategoryWithParent_returnsMapWithParentInfo() {
        Object[] row = {11L, "Смартфоны", 1L, "Электроника"};
        TypedQuery<Object[]> q = stubTypedQuery(List.<Object[]>of(row));
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(q);

        Map<String, Object> result = repo.getSubcategoryWithParent("Смартфоны");

        assertThat(result).containsEntry("id", 11L)
                .containsEntry("name", "Смартфоны")
                .containsEntry("parent_id", 1L)
                .containsEntry("parent_name", "Электроника");
    }

    @Test
    void getSubcategoryWithParent_returnsNullWhenAbsent() {
        TypedQuery<Object[]> q = stubTypedQuery(List.of());
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(q);

        assertThat(repo.getSubcategoryWithParent("Несуществующее")).isNull();
    }
}
