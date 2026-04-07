package com.mipt.portal.repository;

import com.mipt.portal.entity.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepository {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<Comment> commentRowMapper = (rs, rowNum) -> {
    Comment comment = new Comment();
    comment.setId(rs.getLong("id"));
    comment.setAdvertisementId(rs.getLong("ad_id"));
    comment.setUserId(rs.getLong("user_id"));
    comment.setUserName(rs.getString("user_name"));
    comment.setContent(rs.getString("content"));

    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
    if (timestamp != null) {
      comment.setCreatedAt(timestamp.toLocalDateTime());
    }

    return comment;
  };

  public Comment save(Comment comment) {
    if (comment.getId() == null) {
      String sql = "INSERT INTO comments (ad_id, user_id, user_name, content, created_at) VALUES (?, ?, ?, ?, ?)";
      jdbcTemplate.update(sql,
        comment.getAdvertisementId(),
        comment.getUserId(),
        comment.getUserName(),
        comment.getContent(),
        LocalDateTime.now()
      );
      Long id = jdbcTemplate.queryForObject("SELECT lastval()", Long.class);
      comment.setId(id);
    } else {
      String sql = "UPDATE comments SET ad_id = ?, user_id = ?, user_name = ?, content = ? WHERE id = ?";
      jdbcTemplate.update(sql,
        comment.getAdvertisementId(),
        comment.getUserId(),
        comment.getUserName(),
        comment.getContent(),
        comment.getId()
      );
    }
    return comment;
  }

  public List<Comment> findByAdvertisementIdOrderByCreatedAtDesc(Long advertisementId) {
    String sql = "SELECT * FROM comments WHERE ad_id = ? ORDER BY created_at DESC";
    return jdbcTemplate.query(sql, commentRowMapper, advertisementId);
  }

  public List<Comment> findByAdId(Long adId) {
    return findByAdvertisementIdOrderByCreatedAtDesc(adId);
  }

  public Optional<Comment> findById(Long id) {
    String sql = "SELECT * FROM comments WHERE id = ?";
    try {
      Comment comment = jdbcTemplate.queryForObject(sql, commentRowMapper, id);
      return Optional.ofNullable(comment);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public long countByAdvertisementId(Long advertisementId) {
    String sql = "SELECT COUNT(*) FROM comments WHERE ad_id = ?";
    return jdbcTemplate.queryForObject(sql, Long.class, advertisementId);
  }

  public void deleteById(Long id) {
    String sql = "DELETE FROM comments WHERE id = ?";
    jdbcTemplate.update(sql, id);
  }

  public void deleteByAdvertisementId(Long advertisementId) {
    String sql = "DELETE FROM comments WHERE ad_id = ?";
    jdbcTemplate.update(sql, advertisementId);
  }

  public void deleteAllByAdId(Long adId) {
    deleteByAdvertisementId(adId);
  }
}