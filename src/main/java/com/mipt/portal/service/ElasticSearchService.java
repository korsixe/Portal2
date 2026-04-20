package com.mipt.portal.service;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSearchService {

  private final ElasticsearchOperations elasticsearchOperations;
  private final AnnouncementRepository announcementRepository;

  /**
   * Fuzzy-поиск с опечатками.
   * Elasticsearch сам считает расстояние Левенштейна между запросом и полями title/description.
   */
  public List<Announcement> searchWithTypos(String queryText) {
    log.info("Starting Elasticsearch fuzzy search for query: '{}'", queryText);

    if (queryText == null || queryText.trim().isEmpty()) {
      log.warn("Search query is empty. Returning empty list.");
      return List.of();
    }

    Criteria criteria = new Criteria("title").fuzzy(queryText)
      .or("description").fuzzy(queryText);

    CriteriaQuery query = new CriteriaQuery(criteria);
    SearchHits<Announcement> searchHits = elasticsearchOperations.search(query, Announcement.class);

    log.info("Elasticsearch found {} results for query: '{}'", searchHits.getTotalHits(), queryText);

    return searchHits.getSearchHits().stream()
      .map(SearchHit::getContent)
      .toList();
  }

  /**
   * Batch-индексация: берёт все ACTIVE-объявления из Postgres
   * и заливает их в Elasticsearch.
   * Это НАША ручная синхронизация
   */
  @Transactional(readOnly = true)
  public int reindexAll() {
    log.info("Starting full reindex from Postgres to Elasticsearch...");

    IndexOperations indexOps = elasticsearchOperations.indexOps(Announcement.class);
    if (indexOps.exists()) {
      indexOps.delete();
      log.info("Old index deleted");
    }
    indexOps.create();
    indexOps.putMapping(indexOps.createMapping());
    log.info("New index created with mapping");

    List<Announcement> activeAds = announcementRepository.findAllByStatus(AdStatus.ACTIVE);

    if (activeAds.isEmpty()) {
      log.warn("No ACTIVE announcements to index");
      return 0;
    }

    elasticsearchOperations.save(activeAds);

    log.info("Reindexed {} active announcements into Elasticsearch", activeAds.size());
    return activeAds.size();
  }
}