package com.example.soundtribe.dao;

import com.example.soundtribe.manager.CommentManager;
import com.example.soundtribe.entità.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public CommentDAO() {
        initTable();
    }

    private void initTable() {
        // Tabella principale commenti
        String sql = "CREATE TABLE IF NOT EXISTS comments (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "username TEXT, " +
                "content TEXT NOT NULL, " +
                "likes INT DEFAULT 0, " +
                "parent_id INT, " +
                "song_id INT, " +
                "execution_id INT, " +
                "concert_id INT, " +
                "status TEXT DEFAULT 'Pending'" +
                ")";

        // Tabella per tracciare i like (uno per utente per commento)
        String sqlLikes = "CREATE TABLE IF NOT EXISTS comment_likes (" +
                "user_id INT NOT NULL, " +
                "comment_id INT NOT NULL, " +
                "PRIMARY KEY (user_id, comment_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = CredDAO.getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Crea la tabella commenti se non esiste
            stmt.execute(sql);

            // 2. Crea la tabella like se non esiste
            stmt.execute(sqlLikes);

            // 3. MIGRAZIONE COLONNE (Aggiungi se mancano)
            try {
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS execution_id INT");
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS concert_id INT");
                stmt.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'Pending'");
            } catch (SQLException ignore) {}

            // 4. FIX: Rimuovi il vincolo NOT NULL da song_id
            try {
                stmt.execute("ALTER TABLE comments ALTER COLUMN song_id DROP NOT NULL");
            } catch (SQLException e) {
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
        String sql = "INSERT INTO comments (user_id, username, content, parent_id, likes, song_id, execution_id, concert_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            switch (type) {
                case SONG:
                    pstmt.setInt(6, resourceId);
                    pstmt.setNull(7, Types.INTEGER);
                    pstmt.setNull(8, Types.INTEGER);
                    break;
                case EXECUTION:
                    pstmt.setNull(6, Types.INTEGER);
                    pstmt.setInt(7, resourceId);
                    pstmt.setNull(8, Types.INTEGER);
                    break;
                case CONCERT:
                    pstmt.setNull(6, Types.INTEGER);
                    pstmt.setNull(7, Types.INTEGER);
                    pstmt.setInt(8, resourceId);
                    break;
            }
            pstmt.setString(9, comment.getStatus());

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

        String sql = "SELECT * FROM comments WHERE parent_id IS NULL AND " + column + " = ? ORDER BY id ASC";
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

    /**
     * Verifica se l'utente ha già messo like a questo commento.
     * Ritorna true se esiste un like di quell'utente su quel commento.
     */
    public boolean hasUserLiked(int userId, int commentId) {
        String sql = "SELECT 1 FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, commentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // True se esiste un like
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Aggiunge un like. Se l'utente ha già messo like, non fa nulla.
     */
    public void toggleLike(int userId, int commentId) {
        // Se l'utente ha già messo like, non fare nulla
        if (hasUserLiked(userId, commentId)) {
            System.out.println("Utente " + userId + " ha già messo like al commento " + commentId);
            return;
        }

        String sqlInsertLike = "INSERT INTO comment_likes (user_id, comment_id) VALUES (?, ?)";
        String sqlUpdateLikes = "UPDATE comments SET likes = likes + 1 WHERE id = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmtLike = conn.prepareStatement(sqlInsertLike);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateLikes)) {

            // Inserisci il like nella tabella di tracciamento
            pstmtLike.setInt(1, userId);
            pstmtLike.setInt(2, commentId);
            pstmtLike.executeUpdate();

            // Incrementa il contatore di like nel commento
            pstmtUpdate.setInt(1, commentId);
            pstmtUpdate.executeUpdate();

            System.out.println("Like aggiunto dall'utente " + userId + " al commento " + commentId);
        } catch (SQLException e) {
            System.err.println("Errore nel toggle like: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Rimuove un like. Se l'utente non ha messo like, non fa nulla.
     */
    public void removeLike(int userId, int commentId) {
        // Se l'utente non ha messo like, non fare nulla
        if (!hasUserLiked(userId, commentId)) {
            System.out.println("Utente " + userId + " non ha messo like al commento " + commentId);
            return;
        }

        String sqlDeleteLike = "DELETE FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        String sqlUpdateLikes = "UPDATE comments SET likes = likes - 1 WHERE id = ? AND likes > 0";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmtLike = conn.prepareStatement(sqlDeleteLike);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateLikes)) {

            // Rimuovi il like dalla tabella di tracciamento
            pstmtLike.setInt(1, userId);
            pstmtLike.setInt(2, commentId);
            pstmtLike.executeUpdate();

            // Decrementa il contatore di like nel commento
            pstmtUpdate.setInt(1, commentId);
            pstmtUpdate.executeUpdate();

            System.out.println("Like rimosso dall'utente " + userId + " al commento " + commentId);
        } catch (SQLException e) {
            System.err.println("Errore nella rimozione del like: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Integer parentId = rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null;

        int sId = rs.getObject("song_id") != null ? rs.getInt("song_id") : 0;
        int eId = 0;
        int cId = 0;
        String status = "Pending";

        try { eId = rs.getInt("execution_id"); } catch (SQLException e) {}
        try { cId = rs.getInt("concert_id"); } catch (SQLException e) {}
        try { status = rs.getString("status"); } catch (SQLException e) {}

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
                status
        );
    }

    public int getTotalComments() {
        String sql = "SELECT COUNT(*) FROM comments";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del numero totale di commenti: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
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

    public List<Comment> getPendingComments() {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE status = 'Pending' ORDER BY id DESC";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Comment c = mapRow(rs);
                comments.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei commenti in sospeso: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    public void updateCommentStatus(int commentId, String newStatus) {
        String sql = "UPDATE comments SET status = ? WHERE id = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, commentId);
            pstmt.executeUpdate();

            System.out.println("Commento " + commentId + " aggiornato a: " + newStatus);
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento dello stato del commento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteComment(int commentId) {
        String sql = "DELETE FROM comments WHERE id = ? OR parent_id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, commentId);
            pstmt.executeUpdate();
            System.out.println("Commento " + commentId + " e le relative risposte eliminati.");
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione del commento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isResourceOwner(CommentManager.ResourceType type, int resourceId, int userId) {
        String table = "";
        String idColumn = "";

        switch (type) {
            case SONG:
                table = "songs";
                idColumn = "id";
                break;
            case EXECUTION:
                table = "media_files";
                idColumn = "id";
                break;
            case CONCERT:
                table = "concerts";
                idColumn = "id";
                break;
        }

        String sql = "SELECT uploader_id FROM " + table + " WHERE " + idColumn + " = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, resourceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int uploaderId = rs.getInt("uploader_id");
                    return uploaderId == userId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella verifica della proprietà della risorsa: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Verifica se l'utente può eliminare il commento.
     * Può eliminare se:
     * 1. È l'autore del commento, oppure
     * 2. È il proprietario della risorsa (song, execution, concert)
     */
    public boolean canDeleteComment(int commentId, int userId) {
        String sql = "SELECT user_id, song_id, execution_id, concert_id FROM comments WHERE id = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, commentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int commentAuthorId = rs.getInt("user_id");
                    int songId = rs.getObject("song_id") != null ? rs.getInt("song_id") : 0;
                    int executionId = rs.getObject("execution_id") != null ? rs.getInt("execution_id") : 0;
                    int concertId = rs.getObject("concert_id") != null ? rs.getInt("concert_id") : 0;

                    // È l'autore del commento?
                    if (commentAuthorId == userId) {
                        return true;
                    }

                    // È il proprietario della risorsa?
                    if (songId > 0 && isResourceOwner(CommentManager.ResourceType.SONG, songId, userId)) {
                        return true;
                    }
                    if (executionId > 0 && isResourceOwner(CommentManager.ResourceType.EXECUTION, executionId, userId)) {
                        return true;
                    }
                    if (concertId > 0 && isResourceOwner(CommentManager.ResourceType.CONCERT, concertId, userId)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella verifica dei permessi di eliminazione: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}