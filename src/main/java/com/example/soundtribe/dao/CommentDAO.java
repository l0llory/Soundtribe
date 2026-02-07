package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private String dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";
    private String user = "postgres";
    private String password = "AppSoundtribe14";

    public CommentDAO() {
        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    private void initTable() {
        // Tabella Commenti
        String sqlComments = "CREATE TABLE IF NOT EXISTS comments (" +
                "id SERIAL PRIMARY KEY, " +
                "song_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "username VARCHAR(100), " +
                "content TEXT NOT NULL, " +
                "likes INT DEFAULT 0, " +
                "parent_id INT, " +
                "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE" +
                ")";

        // Tabella Likes (Per gestire un solo like per utente)
        String sqlLikes = "CREATE TABLE IF NOT EXISTS comment_likes (" +
                "user_id INT NOT NULL, " +
                "comment_id INT NOT NULL, " +
                "PRIMARY KEY (user_id, comment_id), " +
                "FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlComments);
            stmt.execute(sqlLikes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Aggiungi commento
    public void addComment(Comment c) {
        String sql = "INSERT INTO comments (song_id, user_id, username, content, likes, parent_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getSongId());
            pstmt.setInt(2, c.getUserId());
            pstmt.setString(3, c.getUsername());
            pstmt.setString(4, c.getContent());
            pstmt.setInt(5, 0);
            if (c.getParentId() == null) pstmt.setNull(6, Types.INTEGER);
            else pstmt.setInt(6, c.getParentId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- GESTIONE LIKE (TOGGLE) ---

    // Controlla se l'utente ha già messo like
    public boolean hasUserLiked(int userId, int commentId) {
        String sql = "SELECT 1 FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, commentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mette o toglie il like
    public void toggleLike(int userId, int commentId) {
        if (hasUserLiked(userId, commentId)) {
            // Se c'è già, lo togliamo (UNLIKE)
            removeLike(userId, commentId);
        } else {
            // Se non c'è, lo mettiamo (LIKE)
            addLike(userId, commentId);
        }
    }

    private void addLike(int userId, int commentId) {
        String sqlLink = "INSERT INTO comment_likes (user_id, comment_id) VALUES (?, ?)";
        String sqlCount = "UPDATE comments SET likes = likes + 1 WHERE id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Transazione
            try (PreparedStatement p1 = conn.prepareStatement(sqlLink);
                 PreparedStatement p2 = conn.prepareStatement(sqlCount)) {

                p1.setInt(1, userId);
                p1.setInt(2, commentId);
                p1.executeUpdate();

                p2.setInt(1, commentId);
                p2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeLike(int userId, int commentId) {
        String sqlLink = "DELETE FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        String sqlCount = "UPDATE comments SET likes = likes - 1 WHERE id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(sqlLink);
                 PreparedStatement p2 = conn.prepareStatement(sqlCount)) {

                p1.setInt(1, userId);
                p1.setInt(2, commentId);
                p1.executeUpdate();

                p2.setInt(1, commentId);
                p2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Recupera l'albero completo dei commenti (Padri + Figli ricorsivi)
    public List<Comment> getFullCommentTree(int songId) {
        List<Comment> roots = new ArrayList<>();
        // Prendi solo i commenti RADICE (parent_id NULL)
        String sql = "SELECT * FROM comments WHERE song_id = ? AND parent_id IS NULL ORDER BY likes DESC, id DESC"; // Ordina per popolarità

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, songId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    loadRepliesRecursive(c, conn); // Carica figli
                    roots.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roots;
    }

    // Metodo ricorsivo per caricare le risposte
    private void loadRepliesRecursive(Comment parent, Connection conn) throws SQLException {
        String sql = "SELECT * FROM comments WHERE parent_id = ? ORDER BY id ASC"; // Ordine cronologico
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parent.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment child = mapRow(rs);
                    parent.addReply(child); // Aggiungi alla lista del padre
                    loadRepliesRecursive(child, conn); // Cerca nipoti
                }
            }
        }
    }

    // Per compatibilità con la vecchia vista "Top 3" (senza ricorsione visiva ma recupero dati)
    public List<Comment> getTop3RootComments(int songId) {
        // ... (stesso codice di prima, o puoi usare getFullCommentTree e limitare la lista nel controller)
        // Per semplicità qui riuso la logica di getFullCommentTree ma limitata
        List<Comment> roots = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE song_id = ? AND parent_id IS NULL ORDER BY likes DESC LIMIT 3";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, songId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    loadRepliesRecursive(c, conn); // Carica comunque le risposte per farle vedere
                    roots.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roots;
    }


    private Comment mapRow(ResultSet rs) throws SQLException {
        Integer parentId = rs.getInt("parent_id");
        if (rs.wasNull()) parentId = null;

        return new Comment(
                rs.getInt("id"),
                rs.getInt("song_id"),
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("content"),
                rs.getInt("likes"),
                parentId
        );
    }
}