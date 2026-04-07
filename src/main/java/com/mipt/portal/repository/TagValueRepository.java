package com.mipt.portal.repository;

import com.mipt.portal.entity.TagValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagValueRepository {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<TagValue> tagValueRowMapper = (rs, rowNum) -> {
    TagValue tagValue = new TagValue();
    tagValue.setId(rs.getLong("id"));
    tagValue.setValue(rs.getString("value"));
    return tagValue;
  };

  public List<TagValue> findAll() {
    String sql = "SELECT * FROM tag_values ORDER BY value";
    return jdbcTemplate.query(sql, tagValueRowMapper);
  }

  public Optional<TagValue> findById(Long id) {
    String sql = "SELECT * FROM tag_values WHERE id = ?";
    try {
      TagValue tagValue = jdbcTemplate.queryForObject(sql, tagValueRowMapper, id);
      return Optional.ofNullable(tagValue);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public List<TagValue> findByTagId(Long tagId) {
    String sql = "SELECT * FROM tag_values WHERE tag_id = ? ORDER BY value";
    return jdbcTemplate.query(sql, tagValueRowMapper, tagId);
  }

  public Optional<TagValue> findByTagIdAndValue(Long tagId, String value) {
    String sql = "SELECT * FROM tag_values WHERE tag_id = ? AND value = ?";
    try {
      TagValue tagValue = jdbcTemplate.queryForObject(sql, tagValueRowMapper, tagId, value);
      return Optional.ofNullable(tagValue);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public TagValue save(TagValue tagValue) {
    if (tagValue.getId() == null) {
      String sql = "INSERT INTO tag_values (tag_id, value, created_at) VALUES (?, ?, ?)";
      jdbcTemplate.update(sql, tagValue.getTag().getId(), tagValue.getValue(), LocalDateTime.now());
      Long id = jdbcTemplate.queryForObject("SELECT lastval()", Long.class);
      tagValue.setId(id);
    } else {
      String sql = "UPDATE tag_values SET tag_id = ?, value = ? WHERE id = ?";
      jdbcTemplate.update(sql, tagValue.getTag().getId(), tagValue.getValue(), tagValue.getId());
    }
    return tagValue;
  }

  public void deleteById(Long id) {
    String sql = "DELETE FROM tag_values WHERE id = ?";
    jdbcTemplate.update(sql, id);
  }

  public void deleteByTagId(Long tagId) {
    String sql = "DELETE FROM tag_values WHERE tag_id = ?";
    jdbcTemplate.update(sql, tagId);
  }

  public int countByTagId(Long tagId) {
    String sql = "SELECT COUNT(*) FROM tag_values WHERE tag_id = ?";
    return jdbcTemplate.queryForObject(sql, Integer.class, tagId);
  }
}