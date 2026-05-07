package com.mipt.portal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomTagRepositoryImpl}.
 *
 * <p>The class uses native PostgreSQL queries with {@code jsonb} casts that don't run on
 * H2; we therefore mock {@link EntityManager} + {@link Query} and exercise both happy
 * paths and every error branch (NoResultException, JSON parse failure, missing ad,
 * invalid id, sql update returning zero).</p>
 */
class CustomTagRepositoryImplTest {

    private EntityManager em;
    private CustomTagRepositoryImpl repo;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        repo = new CustomTagRepositoryImpl();
        ReflectionTestUtils.setField(repo, "em", em);
    }

    private Query stubNativeQuery() {
        Query q = mock(Query.class);
        when(q.setParameter(anyInt(), any())).thenReturn(q);
        return q;
    }

    @Test
    void getTagsWithValues_groupsValuesUnderTag_andSkipsRowsWithoutValue() {
        Object[] r1 = {1L, "Бренд", 10L, "Apple"};
        Object[] r2 = {1L, "Бренд", 11L, "Sony"};
        Object[] r3 = {2L, "Цвет", null, null};       // тег без значений
        Query q = stubNativeQuery();
        when(q.getResultList()).thenReturn(List.of(r1, r2, r3));
        when(em.createNativeQuery(anyString())).thenReturn(q);

        List<Map<String, Object>> result = repo.getTagsWithValues();

        assertThat(result).hasSize(2);
        Map<String, Object> brand = result.get(0);
        assertThat(brand).containsEntry("id", 1L).containsEntry("name", "Бренд");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> brandValues = (List<Map<String, Object>>) brand.get("values");
        assertThat(brandValues).hasSize(2);

        Map<String, Object> color = result.get(1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> colorValues = (List<Map<String, Object>>) color.get("values");
        assertThat(colorValues).isEmpty();
    }

    @Test
    void getAvailableTagsForSubcategory_delegatesToGetTagsWithValues() {
        Query q = stubNativeQuery();
        when(q.getResultList()).thenReturn(List.of());
        when(em.createNativeQuery(anyString())).thenReturn(q);

        List<Map<String, Object>> result = repo.getAvailableTagsForSubcategory("Смартфоны");
        assertThat(result).isEmpty();
    }

    @Test
    void getTagsForAd_returnsParsedJson_whenPresent() {
        Query q = stubNativeQuery();
        when(q.getSingleResult()).thenReturn(
                "[{\"id\":1,\"name\":\"Apple\"},{\"id\":2,\"name\":\"Sony\"}]");
        when(em.createNativeQuery(anyString())).thenReturn(q);

        List<Map<String, Object>> result = repo.getTagsForAd(42L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("name", "Apple");
    }

    @Test
    void getTagsForAd_blankJson_returnsEmptyList() {
        Query q = stubNativeQuery();
        when(q.getSingleResult()).thenReturn("   ");
        when(em.createNativeQuery(anyString())).thenReturn(q);

        assertThat(repo.getTagsForAd(42L)).isEmpty();
    }

    @Test
    void getTagsForAd_nullJson_returnsEmptyList() {
        Query q = stubNativeQuery();
        when(q.getSingleResult()).thenReturn(null);
        when(em.createNativeQuery(anyString())).thenReturn(q);

        assertThat(repo.getTagsForAd(42L)).isEmpty();
    }

    @Test
    void getTagsForAd_noResultException_returnsEmptyList() {
        Query q = stubNativeQuery();
        when(q.getSingleResult()).thenThrow(new NoResultException("none"));
        when(em.createNativeQuery(anyString())).thenReturn(q);

        assertThat(repo.getTagsForAd(42L)).isEmpty();
    }

    @Test
    void getTagsForAd_invalidJson_logsAndReturnsEmptyList() {
        Query q = stubNativeQuery();
        when(q.getSingleResult()).thenReturn("not really json");
        when(em.createNativeQuery(anyString())).thenReturn(q);

        assertThat(repo.getTagsForAd(42L)).isEmpty();
    }

    @Test
    void saveAdTags_nullId_throws() {
        assertThatThrownBy(() -> repo.saveAdTags(null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ad ID");
    }

    @Test
    void saveAdTags_zeroOrNegativeId_throws() {
        assertThatThrownBy(() -> repo.saveAdTags(0L, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveAdTags_adNotFound_throws() {
        Query countQ = stubNativeQuery();
        when(countQ.getSingleResult()).thenReturn(0L);
        when(em.createNativeQuery(anyString())).thenReturn(countQ);

        assertThatThrownBy(() -> repo.saveAdTags(99L, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ad not found");
    }

    @Test
    void saveAdTags_nullCount_throws() {
        Query countQ = stubNativeQuery();
        when(countQ.getSingleResult()).thenReturn(null);
        when(em.createNativeQuery(anyString())).thenReturn(countQ);

        assertThatThrownBy(() -> repo.saveAdTags(99L, List.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void saveAdTags_happyPath_executesUpdate() {
        Query countQ = stubNativeQuery();
        when(countQ.getSingleResult()).thenReturn(1L);

        Query updateQ = stubNativeQuery();
        when(updateQ.executeUpdate()).thenReturn(1);

        when(em.createNativeQuery(anyString())).thenReturn(countQ, updateQ);

        repo.saveAdTags(42L, List.of(Map.of("id", 1, "name", "Apple")));

        verify(updateQ).executeUpdate();
    }

    @Test
    void saveAdTags_jsonSerializationFails_wrapsAsRuntimeException() {
        Query countQ = stubNativeQuery();
        when(countQ.getSingleResult()).thenReturn(1L);
        when(em.createNativeQuery(anyString())).thenReturn(countQ);

        // объект с геттером, бросающим checked-исключение IOException →
        // Jackson обернёт его в JsonMappingException (extends IOException extends Exception).
        // Это попадает в `catch (Exception e)` и проходит ветку «обернуть в RuntimeException»
        java.util.Map<String, Object> bad = new java.util.HashMap<>();
        bad.put("bad", new BadJsonBean());
        java.util.List<java.util.Map<String, Object>> badList = java.util.List.of(bad);

        assertThatThrownBy(() -> repo.saveAdTags(42L, badList))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error serializing tags to JSON");
    }

    /** Bean с геттером, бросающим checked-исключение, чтобы заставить Jackson
     *  выбросить JsonMappingException из {@code writeValueAsString}. */
    public static class BadJsonBean {
        public String getValue() throws java.io.IOException {
            throw new java.io.IOException("simulated");
        }
    }

    @Test
    void saveAdTags_zeroRowsAffected_throws() {
        Query countQ = stubNativeQuery();
        when(countQ.getSingleResult()).thenReturn(1L);

        Query updateQ = stubNativeQuery();
        when(updateQ.executeUpdate()).thenReturn(0);

        when(em.createNativeQuery(anyString())).thenReturn(countQ, updateQ);

        assertThatThrownBy(() -> repo.saveAdTags(42L, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update");
    }
}
