package com.mipt.portal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CustomTagRepositoryImpl implements CustomTagRepository {

  private static final Logger LOG = LoggerFactory.getLogger(CustomTagRepositoryImpl.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @PersistenceContext
  private EntityManager em;

  @Override
  public List<Map<String, Object>> getTagsWithValues() {
    List<Object[]> rows = em.createNativeQuery(
            """
            SELECT t.id, t.name, tv.id, tv.value
            FROM tags t
            LEFT JOIN tag_values tv ON tv.tag_id = t.id
            ORDER BY t.name, tv.value
            """
        )
        .getResultList();

    Map<Long, Map<String, Object>> tagsMap = new LinkedHashMap<>();

    for (Object[] row : rows) {
      Long tagId = ((Number) row[0]).longValue();
      Map<String, Object> tag = tagsMap.computeIfAbsent(tagId, ignored -> {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", tagId);
        result.put("name", String.valueOf(row[1]));
        result.put("values", new ArrayList<Map<String, Object>>());
        return result;
      });

      if (row[2] != null) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", ((Number) row[2]).longValue());
        value.put("name", String.valueOf(row[3]));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
        values.add(value);
      }
    }

    List<Map<String, Object>> result = new ArrayList<>(tagsMap.values());
    LOG.info("Loaded {} tags from database", result.size());
    return result;
  }

  @Override
  public List<Map<String, Object>> getAvailableTagsForSubcategory(String subcategoryName) {
    return getTagsWithValues();
  }

  @Override
  public List<Map<String, Object>> getTagsForAd(Long adId) {
    try {
      String tagsJson = (String) em.createNativeQuery("SELECT tags::text FROM ads WHERE id = ?1")
          .setParameter(1, adId)
          .getSingleResult();

      if (tagsJson != null && !tagsJson.trim().isEmpty()) {
        return OBJECT_MAPPER.readValue(
            tagsJson,
            OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class)
        );
      }
    } catch (NoResultException e) {
      return new ArrayList<>();
    } catch (Exception e) {
      LOG.error("Error getting tags for ad {}", adId, e);
    }
    return new ArrayList<>();
  }

  @Override
  @Transactional
  public void saveAdTags(Long adId, List<Map<String, Object>> tagSelections) {
    if (adId == null || adId <= 0) {
      throw new IllegalArgumentException("Invalid ad ID");
    }

    Number count = (Number) em.createNativeQuery("SELECT COUNT(*) FROM ads WHERE id = ?1")
        .setParameter(1, adId)
        .getSingleResult();
    if (count == null || count.longValue() == 0) {
      throw new RuntimeException("Ad not found with ID: " + adId);
    }

    try {
      String tagsJson = OBJECT_MAPPER.writeValueAsString(tagSelections);
      String sql = "UPDATE ads "
          + "SET tags = CAST(?1 AS jsonb), "
          + "tags_count = ?2, "
          + "updated_at = CURRENT_TIMESTAMP "
          + "WHERE id = ?3";

      Query query = em.createNativeQuery(sql);
      query.setParameter(1, tagsJson);
      query.setParameter(2, tagSelections.size());
      query.setParameter(3, adId);

      int updatedRows = query.executeUpdate();

      if (updatedRows == 0) {
        throw new RuntimeException("Failed to update tags for ad " + adId);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error serializing tags to JSON for ad " + adId, e);
    }
  }
}
