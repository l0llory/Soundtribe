package com.example.soundtribe.dao;

import com.example.soundtribe.CommentManager;
import com.example.soundtribe.entità.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public CommentDAO() {
        initTable();
    }

    private void initTable() {
        // Definizione base (utile solo per la prima creazione)
        String sql = "CREATE TABLE IF NOT EXISTS comments (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "username TEXT, " +
                "content TEXT NOT NULL, " +
                "likes INT DEFAULT 0, " +
                "parent_id INT, " +
                "song_id INT, " +          // Ora deve essere nullable
                "execution_id INT, " +
                "concert_id INT " +
                ")";

        try (Connection conn = CredDAO.getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Crea la tabella se non esiste
            stmt.execute(sql);

            // 2. MIGRAZIONE COLONNE (Aggiungi se mancano)
            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS execution_id INT");
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS concert_id INT");
            } catch (SQLException ignore) {}

            // 3. FIX IMPORTANTE: Rimuovi il vincolo NOT NULL da song_id
            // Questo permette di inserire commenti per Concerti ed Esecuzioni lasciando song_id vuoto.
            try {
                stmt.execute("ALTER TABLE comments ALTER COLUMN song_id DROP NOT NULL");
            } catch (SQLException e) {
                // Ignoriamo l'errore se è già nullable o se c'è un problema specifico di versione,
                // ma stampiamo un warning per debug.
                System.out.println("Info: Tentativo di rendere song_id nullable. " + e.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aggiunge un commento al database collegandolo alla risorsa corretta.
     */
    public void addComment(Comment comment, CommentManager.ResourceType type, int resourceId) {
        String sql = "INSERT INTO comments (user_id, username, content, parent_id, likes, song_id, execution_id, concert_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, comment.getUserId());
            pstmt.setString(2, comment.getUsername());
            pstmt.setString(3, comment.getContent());

            if (comment.getParentId() != null && comment.getParentId() > 0) {
                pstmt.setInt(4, comment.getParentId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setInt(5, 0); // Likes

            // LOGICA DI ASSEGNAZIONE ID
            switch (type) {
                case SONG:
                    pstmt.setInt(6, resourceId);      // song_id
                    pstmt.setNull(7, Types.INTEGER);
                    pstmt.setNull(8, Types.INTEGER);
                    break;
                case EXECUTION:
                    pstmt.setNull(6, Types.INTEGER);  // song_id NULL
                    pstmt.setInt(7, resourceId);      // execution_id
                    pstmt.setNull(8, Types.INTEGER);
                    break;
                case CONCERT:
                    pstmt.setNull(6, Types.INTEGER);  // song_id NULL
                    pstmt.setNull(7, Types.INTEGER);
                    pstmt.setInt(8, resourceId);      // concert_id
                    break;
            }

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore inserimento commento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addComment(Comment comment) {
        if (comment.getSongId() > 0) addComment(comment, CommentManager.ResourceType.SONG, comment.getSongId());
        else if (comment.getExecutionId() > 0) addComment(comment, CommentManager.ResourceType.EXECUTION, comment.getExecutionId());
        else if (comment.getConcertId() > 0) addComment(comment, CommentManager.ResourceType.CONCERT, comment.getConcertId());
    }

    public List<Comment> getCommentsByResource(CommentManager.ResourceType type, int resourceId) {
        List<Comment> comments = new ArrayList<>();

        String column = "";
        switch (type) {
            case SONG -> column = "song_id";
            case EXECUTION -> column = "execution_id";
            case CONCERT -> column = "concert_id";
        }

        String sql = "SELECT * FROM comments ORDER BY id ASC";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, resourceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapRow(rs);
                    c.setReplies(getReplies(c.getId()));
                    comments.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    private List<Comment> getReplies(int parentId) {
        List<Comment> replies = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE parent_id = ? ORDER BY id ASC";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    public List<Comment> getTop3RootComments(int songId) {
        return getCommentsByResource(CommentManager.ResourceType.SONG, songId);
    }

    public boolean hasUserLiked(int userId, int commentId) {
        return false;
    }

    public void toggleLike(int userId, int commentId) {
        String sql = "UPDATE comments SET likes = likes + 1 WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Integer parentId = rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null;

        int sId = rs.getObject("song_id") != null ? rs.getInt("song_id") : 0;
        int eId = 0;
        int cId = 0;

        try { eId = rs.getInt("execution_id"); } catch (SQLException e) {}
        try { cId = rs.getInt("concert_id"); } catch (SQLException e) {}

        return new Comment(
                rs.getInt("id"),
                sId,
                eId,
                cId,
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("content"),
                rs.getInt("likes"),
                parentId,
               rs.getString("status")
        );
    }
    // All'interno di CommentDAO.java

    public int getTotalComments() {
        String sql = "SELECT COUNT(*) FROM comments";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1); // Il primo (e unico) risultato della query COUNT(*)
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del numero totale di commenti: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // In caso di errore o nessun commento
    }

    public int getCommentsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM comments WHERE status = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del numero di commenti per stato '" + status + "': " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}