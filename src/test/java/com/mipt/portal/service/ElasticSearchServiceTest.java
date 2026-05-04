package com.mipt.portal.service;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSearchServiceTest {

  @Mock private ElasticsearchOperations operations;
  @Mock private AnnouncementRepository repo;
  @InjectMocks private ElasticSearchService service;

  @Test
  void searchWithTypos_emptyForBlank() {
    assertThat(service.searchWithTypos("")).isEmpty();
    assertThat(service.searchWithTypos(null)).isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Test
  void searchWithTypos_returnsContent() {
    Announcement ad = new Announcement();
    SearchHit<Announcement> hit = mock(SearchHit.class);
    when(hit.getContent()).thenReturn(ad);
    SearchHits<Announcement> hits = mock(SearchHits.class);
    when(hits.getSearchHits()).thenReturn(List.of(hit));
    when(hits.getTotalHits()).thenReturn(1L);
    when(operations.search(any(CriteriaQuery.class), any(Class.class))).thenReturn(hits);

    List<Announcement> results = service.searchWithTypos("query");
    assertThat(results).containsExactly(ad);
  }

  @Test
  void reindexAll_returnsZeroWhenNoActiveAds() {
    IndexOperations indexOps = mock(IndexOperations.class);
    when(operations.indexOps(Announcement.class)).thenReturn(indexOps);
    when(indexOps.exists()).thenReturn(false);
    when(indexOps.createMapping()).thenReturn(Document.create());
    when(repo.findAllByStatus(AdStatus.ACTIVE)).thenReturn(List.of());
    int result = service.reindexAll();
    assertThat(result).isZero();
  }

  @SuppressWarnings("unchecked")
  @Test
  void reindexAll_savesAndReturnsCount() {
    IndexOperations indexOps = mock(IndexOperations.class);
    when(operations.indexOps(Announcement.class)).thenReturn(indexOps);
    when(indexOps.exists()).thenReturn(true);
    when(indexOps.createMapping()).thenReturn(Document.create());

    Announcement a1 = new Announcement();
    Announcement a2 = new Announcement();
    when(repo.findAllByStatus(AdStatus.ACTIVE)).thenReturn(List.of(a1, a2));

    int result = service.reindexAll();
    assertThat(result).isEqualTo(2);
    verify(indexOps).delete();
    verify(indexOps).create();
    verify(operations).save((Iterable<Announcement>) any());
  }
}
