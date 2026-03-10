package com.mipt.portal.annoucementContent.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepository {

  // private final JdbcTemplate jdbcTemplate;

  public Comment save(Comment comment) {
        /*
        String sql = "INSERT INTO comments (ad_id, user_id, user_name, content, created_at) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comment.getAdvertisementId());
            ps.setLong(2, comment.getUserId());
            ps.setString(3, comment.getAuthor());
            ps.setString(4, comment.getText());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        comment.setId(keyHolder.getKey().longValue());
        log.info("✅ Комментарий сохранен с ID: {}", comment.getId());
        return comment;
        */
    log.info("✅ Комментарий сохранен (заглушка)");
    comment.setId(1L);
    return comment;
  }

  public Comment findById(Long id) {
        /*
        String sql = "SELECT * FROM comments WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Comment comment = new Comment();
            comment.setId(rs.getLong("id"));
            comment.setAdvertisementId(rs.getLong("ad_id"));
            comment.setUserId(rs.getLong("user_id"));
            comment.setAuthor(rs.getString("user_name"));
            comment.setText(rs.getString("content"));
            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return comment;
        }, id);
        */
    log.info("✅ Комментарий найден (заглушка) ID: {}", id);
    Comment comment = new Comment();
    comment.setId(id);
    comment.setAdvertisementId(1L);
    comment.setUserId(1L);
    comment.setAuthor("Test Author");
    comment.setText("Test content");
    comment.setCreatedAt(LocalDateTime.now());
    return comment;
  }

  public List<Comment> findByAdId(Long advertisementId) {
        /*
        String sql = "SELECT * FROM comments WHERE ad_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Comment comment = new Comment();
            comment.setId(rs.getLong("id"));
            comment.setAdvertisementId(rs.getLong("ad_id"));
            comment.setUserId(rs.getLong("user_id"));
            comment.setAuthor(rs.getString("user_name"));
            comment.setText(rs.getString("content"));
            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return comment;
        }, advertisementId);
        */
    log.info("✅ Комментарии найдены (заглушка) для adId: {}", advertisementId);
    return List.of();
  }

  public int updateText(Long id, String newText) {
        /*
        String sql = "UPDATE comments SET content = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newText, id);
        */
    log.info("✅ Комментарий обновлен (заглушка) ID: {}", id);
    return 1;
  }

  public int deleteById(Long id) {
        /*
        String sql = "DELETE FROM comments WHERE id = ?";
        return jdbcTemplate.update(sql, id);
        */
    log.info("✅ Комментарий удален (заглушка) ID: {}", id);
    return 1;
  }

  public long countByAdId(Long advertisementId) {
        /*
        String sql = "SELECT COUNT(*) FROM comments WHERE ad_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, advertisementId);
        */
    log.info("✅ Подсчет комментариев (заглушка) для adId: {}", advertisementId);
    return 5;
  }
}