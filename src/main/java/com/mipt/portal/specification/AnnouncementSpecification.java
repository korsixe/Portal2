package com.mipt.portal.specification;

import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementSpecification {

  public static Specification<Announcement> build(AnnouncementFilterDto filter, AdStatus requiredStatus) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (requiredStatus != null) {
        predicates.add(cb.equal(root.get("status"), requiredStatus));
      }

      if (filter.getText() != null && !filter.getText().isBlank()) {
        String pattern = "%" + filter.getText().toLowerCase() + "%";
        Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
        Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
        predicates.add(cb.or(titleMatch, descMatch));
      }

      if (filter.getMinPrice() != null) {
        predicates.add(cb.ge(root.get("price"), filter.getMinPrice()));
      }

      if (filter.getMaxPrice() != null) {
        predicates.add(cb.le(root.get("price"), filter.getMaxPrice()));
      }

      if (filter.getCategory() != null) {
        predicates.add(cb.equal(root.get("category"), filter.getCategory()));
      }

      if (filter.getSubcategory() != null && !filter.getSubcategory().isBlank()) {
        predicates.add(cb.equal(root.get("subcategory"), filter.getSubcategory()));
      }

      if (filter.getCondition() != null) {
        predicates.add(cb.equal(root.get("condition"), filter.getCondition()));
      }

      if (filter.getCreatedAfter() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), cb.literal(filter.getCreatedAfter())));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}