package com.example.soundtribe.dao;

import com.example.soundtribe.entita.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public UserDAO() {
        initTable();
    }

    // Crea la tabella utenti se non esiste e aggiorna lo schema
    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "surname VARCHAR(100) NOT NULL, " +
                "email VARCHAR(150) UNIQUE NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "is_admin BOOLEAN DEFAULT FALSE, " +
                "status VARCHAR(20) DEFAULT 'Pending' CHECK (status IN ('Pending', 'Verified', 'Banned')), " +
                "profile_pic_path TEXT, " +
                "favorite_genre TEXT, " +
                "motivation TEXT" +
                ")";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Crea la tabella base se non c'è
            stmt.execute(sql);

            // 2. Migrazione sicura per database esistenti
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_pic_path TEXT");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS favorite_genre TEXT");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS motivation TEXT");
                // Aggiunge la colonna status se non esiste già
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'Pending'");
            } catch (SQLException ignore) {
                // Colonne già presenti
            }

        } catch (SQLException e) {
            System.err.println("Errore creazione/aggiornamento tabella users: " + e.getMessage());
        }
    }

    // --- METODI CRUD PRINCIPALI ---

    // 1. REGISTRAZIONE (Create)
    public boolean registerUser(User user) {
        // Inseriamo il campo status al posto di is_approved
        String sql = "INSERT INTO users (name, surname, email, password, is_admin, status, favorite_genre, profile_pic_path, motivation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getSurname());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setBoolean(5, user.isAdmin());
            pstmt.setString(6, user.getStatus());     // Passa la stringa ("Pending" di default)
            pstmt.setString(7, user.getFavoriteGenre());
            pstmt.setString(8, user.getProfilePicPath());
            pstmt.setString(9, user.getMotivation());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Errore: Email già registrata.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    // 2. GET ALL USERS (Read - Lista Principale)
    // Ritorna SOLO gli utenti VERIFICATI (status = 'Verified')
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'Verified' ORDER BY name ASC";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 3. GET PENDING USERS (Read - Lista Richieste)
    // Ritorna SOLO gli utenti IN ATTESA (status = 'Pending')
    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'Pending' ORDER BY id ASC";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 4. UPDATE STATUS (Per approvare, rifiutare o bannare un utente)
    // Wrapper per compatibilità
    public void updateUserStatus(int userId, String status) {
        updateUserStatus(userId, status, null);
    }

    // Nuova variante: se lo status è "Banned" e viene fornita una adminReason,
    // aggiorna anche la colonna 'motivation'.
    public void updateUserStatus(int userId, String status, String adminReason) {
        String sql;
        boolean setMotivation = "Banned".equals(status) && adminReason != null;

        if (setMotivation) {
            sql = "UPDATE users SET status = ?, motivation = ? WHERE id = ?";
        } else {
            sql = "UPDATE users SET status = ? WHERE id = ?";
        }

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            if (setMotivation) {
                pstmt.setString(2, adminReason);
                pstmt.setInt(3, userId);
            } else {
                pstmt.setInt(2, userId);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento status utente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 5. UPDATE PROFILE
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, password = ?, profile_pic_path = ?, favorite_genre = ? WHERE id = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getProfilePicPath());
            pstmt.setString(4, user.getFavoriteGenre());
            pstmt.setInt(5, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento del profilo: " + e.getMessage());
            return false;
        }
    }

    // 6. LOGIN
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 7. RICERCA
    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        // Cerca solo tra gli utenti verificati
        String sql = "SELECT * FROM users WHERE status = 'Verified' AND (name ILIKE ? OR surname ILIKE ? OR email ILIKE ?)";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 8. GET BY ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- MAPPER HELPER ---
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setSurname(rs.getString("surname"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setAdmin(rs.getBoolean("is_admin"));
        user.setProfilePicPath(rs.getString("profile_pic_path"));
        user.setFavoriteGenre(rs.getString("favorite_genre"));

        try {
            user.setMotivation(rs.getString("motivation"));
        } catch (SQLException e) {
            user.setMotivation("");
        }

        // Lettura sicura del nuovo campo stringa 'status'
        try {
            user.setStatus(rs.getString("status"));
        } catch (SQLException e) {
            user.setStatus("Pending"); // Fallback di sicurezza
        }

        return user;
    }

    public int getNumberUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del numero totale di utenti: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Corretto per usare PreparedStatement e passare il parametro status
    public int getNumberUsersByStatus(String status){
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del numero  di utenti con stato: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public List<User> getUsersWithReportsAbove(int soglia) {
        List<User> users = new ArrayList<>();

        String sql = "SELECT u.*, COUNT(c.id) AS banned_count " +
                "FROM users u " +
                "JOIN comments c ON u.id = c.user_id " +
                "WHERE c.status = 'Banned' " +
                "GROUP BY u.id " +
                "HAVING COUNT(c.id) >= ? " +
                "ORDER BY banned_count DESC";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, soglia);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = mapRow(rs);
                    user.setBannedCommentsCount(String.valueOf(rs.getInt("banned_count")));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero degli utenti segnalati oltre la soglia: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public String getBannedCommentsCount() {
        String sql = "SELECT COUNT(*) FROM comments WHERE status = 'Banned'";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio globale dei commenti bannati: " + e.getMessage());
            e.printStackTrace();
        }
        return "0";
    }
}