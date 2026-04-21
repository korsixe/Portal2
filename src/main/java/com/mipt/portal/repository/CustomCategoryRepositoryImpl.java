package com.mipt.portal.repository;

import com.mipt.portal.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CustomCategoryRepositoryImpl implements CustomCategoryRepository {

  @PersistenceContext
  private EntityManager em;

  @Override
  public List<Map<String, Object>> getAllCategories() {
    return em.createQuery(
            "select c from Category c where c.parent is null order by c.name",
            Category.class
        ).getResultList().stream()
        .map(category -> {
          Map<String, Object> result = new LinkedHashMap<>();
          result.put("id", category.getId());
          result.put("name", category.getName());
          result.put("isService", Boolean.TRUE.equals(category.getIsService()));
          return result;
        })
        .toList();
  }

  @Override
  public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) {
    return em.createQuery(
            "select c from Category c where c.parent.id = :categoryId order by c.name",
            Category.class
        )
        .setParameter("categoryId", categoryId)
        .getResultList().stream()
        .map(subcategory -> {
          Map<String, Object> result = new LinkedHashMap<>();
          result.put("id", subcategory.getId());
          result.put("name", subcategory.getName());
          return result;
        })
        .toList();
  }

  @Override
  public boolean isServiceSubcategory(Long subcategoryId) {
    List<Boolean> results = em.createQuery(
            """
            select parent.isService
            from Category subcategory
            join subcategory.parent parent
            where subcategory.id = :subcategoryId
            """,
            Boolean.class
        )
        .setParameter("subcategoryId", subcategoryId)
        .getResultList();

    return !results.isEmpty() && Boolean.TRUE.equals(results.get(0));
  }

  @Override
  public Long getParentCategoryIdByName(String subcategoryName) {
    List<Long> results = em.createQuery(
            """
            select parent.id
            from Category subcategory
            join subcategory.parent parent
            where subcategory.name = :subcategoryName
            """,
            Long.class
        )
        .setParameter("subcategoryName", subcategoryName)
        .setMaxResults(1)
        .getResultList();

    return results.isEmpty() ? null : results.get(0);
  }

  @Override
  public Map<String, Object> getSubcategoryWithParent(String subcategoryName) {
    List<Object[]> results = em.createQuery(
            """
            select subcategory.id, subcategory.name, parent.id, parent.name
            from Category subcategory
            left join subcategory.parent parent
            where subcategory.name = :subcategoryName
            """,
            Object[].class
        )
        .setParameter("subcategoryName", subcategoryName)
        .setMaxResults(1)
        .getResultList();

    if (results.isEmpty()) {
      return null;
    }

    Object[] row = results.get(0);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", row[0]);
    result.put("name", row[1]);
    result.put("parent_id", row[2]);
    result.put("parent_name", row[3]);
    return result;
  }
}
