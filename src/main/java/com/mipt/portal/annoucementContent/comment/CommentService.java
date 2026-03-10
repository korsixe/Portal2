package com.mipt.portal.annoucementContent.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  //private final DataSource dataSource;

  @Transactional
  public Comment createComment(Long advertisementId, Long userId, String text) throws SQLException {
        /*
        String sql = "INSERT INTO comments (ad_id, user_id, user_name, content) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String author = getAuthorFromUserId(userId);

            pstmt.setLong(1, advertisementId);
            pstmt.setLong(2, userId);
            pstmt.setString(3, author);
            pstmt.setString(4, text);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            Long generatedCommentId = rs.getLong(1);

            log.info("✅ Комментарий создан: ID={}, adId={}", generatedCommentId, advertisementId);

            return new Comment(generatedCommentId, author, text, LocalDateTime.now(), advertisementId, userId);
        } catch (SQLException e) {
            log.error("❌ Ошибка создания комментария: {}", e.getMessage());
            throw e;
        }
        */
    log.info("✅ Комментарий создан (заглушка): adId={}, userId={}", advertisementId, userId);
    Comment comment = new Comment();
    comment.setId(1L);
    comment.setAdvertisementId(advertisementId);
    comment.setUserId(userId);
    comment.setAuthor("Test Author");
    comment.setText(text);
    comment.setCreatedAt(LocalDateTime.now());
    return comment;
  }

  @Transactional(readOnly = true)
  public Comment getComment(Long commentId) throws SQLException {
        /*
        String sql = "SELECT * FROM comments WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, commentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String author = rs.getString("user_name");
                log.info("✅ Комментарий найден: ID={}", commentId);

                return new Comment(
                    rs.getLong("id"),
                    author,
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getLong("ad_id"),
                    rs.getLong("user_id")
                );
            } else {
                log.warn("⚠️ Комментарий не найден: ID={}", commentId);
                return null;
            }
        } catch (SQLException e) {
            log.error("❌ Ошибка чтения комментария: {}", e.getMessage());
            throw e;
        }
        */
    log.info("✅ Комментарий найден (заглушка): ID={}", commentId);
    Comment comment = new Comment();
    comment.setId(commentId);
    comment.setAdvertisementId(1L);
    comment.setUserId(1L);
    comment.setAuthor("Test Author");
    comment.setText("Test content");
    comment.setCreatedAt(LocalDateTime.now());
    return comment;
  }

  @Transactional
  public void updateComment(Long commentId, String newText) throws SQLException {
        /*
        String sql = "UPDATE comments SET content = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newText);
            pstmt.setLong(2, commentId);

            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                log.info("✅ Комментарий обновлен: ID={}", commentId);
            } else {
                log.warn("⚠️ Комментарий не найден для обновления: ID={}", commentId);
            }
        } catch (SQLException e) {
            log.error("❌ Ошибка обновления комментария: {}", e.getMessage());
            throw e;
        }
        */
    log.info("✅ Комментарий обновлен (заглушка): ID={}", commentId);
  }

  @Transactional
  public boolean deleteComment(Long commentId) throws SQLException {
        /*
        String sql = "DELETE FROM comments WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, commentId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                log.info("✅ Комментарий удален: ID={}", commentId);
                return true;
            } else {
                log.warn("⚠️ Комментарий не найден для удаления: ID={}", commentId);
                return false;
            }
        } catch (SQLException e) {
            log.error("❌ Ошибка удаления комментария: {}", e.getMessage());
            throw e;
        }
        */
    log.info("✅ Комментарий удален (заглушка): ID={}", commentId);
    return true;
  }

  private String getAuthorFromUserId(Long userId) throws SQLException {
        /*
        String sql = "SELECT name FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
            return "Unknown User";
        }
        */
    return "Test Author";
  }
}