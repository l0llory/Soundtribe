package com.example.soundtribe.dao;

import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.manager.CommentManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommentDAO {

    public CommentDAO() {
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS comments (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "likes INT DEFAULT 0, " +
                "parent_id INT, " +
                "song_id INT, " +
                "execution_id INT, " +
                "concert_id INT, " +
                "status TEXT DEFAULT 'Pending'" +
                ")";

        String sqlLikes = "CREATE TABLE IF NOT EXISTS comment_likes (" +
                "user_id INT NOT NULL, " +
                "comment_id INT NOT NULL, " +
                "PRIMARY KEY (user_id, comment_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = CredDAO.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sqlLikes);
            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS execution_id INT");
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS concert_id INT");
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'Pending'");
                stmt.execute("ALTER TABLE comments ALTER COLUMN song_id DROP NOT NULL");
                // Rimuove la colonna username ridondante se ancora presente
                stmt.execute("ALTER TABLE comments DROP COLUMN IF EXISTS username");
            } catch (SQLException ignore) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addComment(Comment comment, CommentManager.ResourceType type, int resourceId) {
        String sql = "INSERT INTO comments (user_id, content, parent_id, likes, song_id, execution_id, concert_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comment.getUserId());
            pstmt.setString(2, comment.getContent());
            if (comment.getParentId() != null && comment.getParentId() > 0) pstmt.setInt(3, comment.getParentId());
            else pstmt.setNull(3, Types.INTEGER);
            pstmt.setInt(4, 0);
            switch (type) {
                case SONG     -> { pstmt.setInt(5, resourceId);    pstmt.setNull(6, Types.INTEGER); pstmt.setNull(7, Types.INTEGER); }
                case EXECUTION -> { pstmt.setNull(5, Types.INTEGER); pstmt.setInt(6, resourceId);    pstmt.setNull(7, Types.INTEGER); }
                case CONCERT   -> { pstmt.setNull(5, Types.INTEGER); pstmt.setNull(6, Types.INTEGER); pstmt.setInt(7, resourceId);    }
            }
            pstmt.setString(8, comment.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addComment(Comment comment) {
        if (comment.getSongId() > 0)       addComment(comment, CommentManager.ResourceType.SONG,      comment.getSongId());
        else if (comment.getExecutionId() > 0) addComment(comment, CommentManager.ResourceType.EXECUTION, comment.getExecutionId());
        else if (comment.getConcertId() > 0)   addComment(comment, CommentManager.ResourceType.CONCERT,   comment.getConcertId());
    }

    public List<Comment> getCommentsByResource(CommentManager.ResourceType type, int resourceId) {
        List<Comment> comments = new ArrayList<>();
        String column = switch (type) {
            case SONG      -> "song_id";
            case EXECUTION -> "execution_id";
            case CONCERT   -> "concert_id";
        };
        String sql = "SELECT c.*, u.name AS author_name " +
                     "FROM comments c LEFT JOIN users u ON c.user_id = u.id " +
                     "WHERE c.parent_id IS NULL AND c." + column + " = ? ORDER BY c.id ASC";
        try (Connection conn = CredDAO.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, resourceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    c.setReplies(getReplies(c.getId()));
                    comments.add(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return comments;
    }

    private List<Comment> getReplies(int parentId) {
        List<Comment> replies = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS author_name " +
                     "FROM comments c LEFT JOIN users u ON c.user_id = u.id " +
                     "WHERE c.parent_id = ? ORDER BY c.id ASC";
        try (Connection conn = CredDAO.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    c.setReplies(getReplies(c.getId()));
                    replies.add(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return replies;
    }

    // --- METODI PER LA MODERAZIONE UTENTE ---

    public Map<String, List<Comment>> getCommentsToModerate(int uploaderId) {
        Map<String, List<Comment>> moderationMap = new LinkedHashMap<>();

        String sqlSongs = "SELECT c.*, s.title AS resource_title, u.name AS author_name " +
                "FROM comments c " +
                "JOIN songs s ON c.song_id = s.id " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "WHERE s.uploader_id = ? AND c.user_id != ? AND c.status != 'Banned' AND c.status != 'Verified' AND c.status != 'Reported' ORDER BY c.id DESC";

        String sqlExecutions = "SELECT c.*, m.title AS resource_title, u.name AS author_name " +
                "FROM comments c " +
                "JOIN media_files m ON c.execution_id = m.id " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "WHERE m.uploader_id = ? AND c.user_id != ? AND c.status != 'Banned' AND c.status != 'Verified' AND c.status != 'Reported' ORDER BY c.id DESC";

        String sqlConcerts = "SELECT c.*, con.title AS resource_title, u.name AS author_name " +
                "FROM comments c " +
                "JOIN concerts con ON c.concert_id = con.id " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "WHERE con.uploader_id = ? AND c.user_id != ? AND c.status != 'Banned' AND c.status != 'Verified' AND c.status != 'Reported' ORDER BY c.id DESC";

        try (Connection conn = CredDAO.getConnection()) {
            fetchAndMapComments(conn, sqlSongs,     uploaderId, "Brano: ",      moderationMap);
            fetchAndMapComments(conn, sqlExecutions, uploaderId, "Esecuzione: ", moderationMap);
            fetchAndMapComments(conn, sqlConcerts,   uploaderId, "Concerto: ",   moderationMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moderationMap;
    }

    private void fetchAndMapComments(Connection conn, String sql, int uploaderId, String prefix, Map<String, List<Comment>> map) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, uploaderId);
            pstmt.setInt(2, uploaderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    String fullTitle = prefix + rs.getString("resource_title");
                    map.putIfAbsent(fullTitle, new ArrayList<>());
                    map.get(fullTitle).add(c);
                }
            }
        }
    }

    // --- GESTIONE LIKES ---

    public boolean hasUserLiked(int userId, int commentId) {
        String sql = "SELECT 1 FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        try (Connection conn = CredDAO.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, commentId);
            try (ResultSet rs = pstmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void toggleLike(int userId, int commentId) {
        if (hasUserLiked(userId, commentId)) return;
        String sqlInsert = "INSERT INTO comment_likes (user_id, comment_id) VALUES (?, ?)";
        String sqlUpdate = "UPDATE comments SET likes = likes + 1 WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement p1 = conn.prepareStatement(sqlInsert);
             PreparedStatement p2 = conn.prepareStatement(sqlUpdate)) {
            p1.setInt(1, userId); p1.setInt(2, commentId); p1.executeUpdate();
            p2.setInt(1, commentId); p2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void removeLike(int userId, int commentId) {
        if (!hasUserLiked(userId, commentId)) return;
        String sqlDel = "DELETE FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        String sqlUpd = "UPDATE comments SET likes = likes - 1 WHERE id = ? AND likes > 0";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement p1 = conn.prepareStatement(sqlDel);
             PreparedStatement p2 = conn.prepareStatement(sqlUpd)) {
            p1.setInt(1, userId); p1.setInt(2, commentId); p1.executeUpdate();
            p2.setInt(1, commentId); p2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteComment(int commentId) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = CredDAO.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- PERMESSI ---

    public boolean isResourceOwner(CommentManager.ResourceType type, int resourceId, int userId) {
        String table = switch (type) {
            case SONG      -> "songs";
            case EXECUTION -> "media_files";
            case CONCERT   -> "concerts";
        };
        String sql = "SELECT uploader_id FROM " + table + " WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, resourceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("uploader_id") == userId;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean canDeleteComment(int commentId, int userId) {
        String sql = "SELECT user_id FROM comments WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id") == userId;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // --- METODI AMMINISTRATIVI ---

    public int getTotalComments() {
        String sql = "SELECT COUNT(*) FROM comments";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getCommentsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM comments WHERE status = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Comment> getCommentsListByStatus(String status) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS author_name " +
                     "FROM comments c LEFT JOIN users u ON c.user_id = u.id " +
                     "WHERE c.status = ? ORDER BY c.id ASC";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    c.setReplies(getReplies(c.getId()));
                    comments.add(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return comments;
    }

    public void updateCommentStatus(int commentId, String newStatus) {
        String sql = "UPDATE comments SET status = ? WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Integer parentId = rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null;
        int sId  = rs.getObject("song_id")      != null ? rs.getInt("song_id")      : 0;
        int eId  = 0, cId = 0;
        String status = "Pending";
        String authorName = null;

        try { eId      = rs.getInt("execution_id"); } catch (SQLException ignore) {}
        try { cId      = rs.getInt("concert_id");   } catch (SQLException ignore) {}
        try { status   = rs.getString("status");     } catch (SQLException ignore) {}
        try { authorName = rs.getString("author_name"); } catch (SQLException ignore) {}

        Comment c = new Comment(
                rs.getInt("id"), sId, eId, cId,
                rs.getInt("user_id"),
                rs.getString("content"), rs.getInt("likes"),
                parentId, status
        );
        c.setAuthorName(authorName);
        return c;
    }
}
