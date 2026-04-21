package com.mipt.portal.repository;

import com.mipt.portal.entity.TagValue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagValueRepository extends JpaRepository<TagValue, Long> {

  @Query("select tv from TagValue tv where tv.tag.id = :tagId order by tv.value")
  List<TagValue> findByTagId(@Param("tagId") Long tagId);

  @Query("select tv from TagValue tv where tv.tag.id = :tagId and tv.value = :value")
  Optional<TagValue> findByTagIdAndValue(@Param("tagId") Long tagId, @Param("value") String value);

  @Modifying
  @Query("delete from TagValue tv where tv.tag.id = :tagId")
  void deleteByTagId(@Param("tagId") Long tagId);

  @Query("select count(tv) from TagValue tv where tv.tag.id = :tagId")
  int countByTagId(@Param("tagId") Long tagId);
}
