package com.example.soundtribe.dao;

import com.example.soundtribe.entità.User;
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
                "is_approved BOOLEAN DEFAULT FALSE, " + // Colonna Approvazione
                "profile_pic_path TEXT, " +
                "favorite_genre TEXT, " +
                "motivation TEXT" + // NUOVA COLONNA
                ")";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Crea la tabella base se non c'è
            stmt.execute(sql);

            // 2. Migrazione sicura: aggiunge colonne se mancano (per database esistenti)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_pic_path TEXT");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS favorite_genre TEXT");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT FALSE");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS motivation TEXT"); // Migrazione Motivazione
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
        // Inseriamo anche i nuovi campi. is_approved sarà FALSE per default.
        String sql = "INSERT INTO users (name, surname, email, password, is_admin, is_approved, favorite_genre, profile_pic_path, motivation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getSurname());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setBoolean(5, user.isAdmin());      // false
            pstmt.setBoolean(6, user.isApproved());   // false (da costruttore)
            pstmt.setString(7, user.getFavoriteGenre());
            pstmt.setString(8, user.getProfilePicPath());
            pstmt.setString(9, user.getMotivation()); // NUOVO

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Codice errore PostgreSQL per "Unique Violation"
                System.err.println("Errore: Email già registrata.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    // 2. GET ALL USERS (Read - Lista Principale)
    // Ritorna SOLO gli utenti APPROVATI (is_approved = TRUE)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_approved = TRUE ORDER BY name ASC";

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
    // Ritorna SOLO gli utenti NON APPROVATI (is_approved = FALSE)
    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_approved = FALSE ORDER BY id ASC";

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

    // 4. UPDATE STATUS (Per approvare utente)
    public void updateUserStatus(int userId, boolean approved) {
        String sql = "UPDATE users SET is_approved = ? WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, approved);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. DELETE USER (Per rifiutare richiesta o eliminare account)
    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 6. UPDATE PROFILE (Aggiornamento utente loggato)
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

    // 7. LOGIN
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User u = mapRow(rs);
                    // Controllo di sicurezza: se non è approvato, login fallito
                    if (!u.isApproved()) {
                        System.out.println("Login bloccato: utente non approvato.");
                        return null;
                    }
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 8. RICERCA
    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        // Cerca solo tra gli utenti approvati
        String sql = "SELECT * FROM users WHERE is_approved = TRUE AND (name ILIKE ? OR surname ILIKE ? OR email ILIKE ?)";

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

    // 9. GET BY ID
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

        // Lettura sicura della motivazione (potrebbe non esistere in vecchi record)
        try {
            user.setMotivation(rs.getString("motivation"));
        } catch (SQLException e) {
            user.setMotivation("");
        }

        try {
            user.setApproved(rs.getBoolean("is_approved"));
        } catch (SQLException e) {
            user.setApproved(true); // Fallback
        }

        return user;
    }
    public static int getNumberUsers(){
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1); // Il primo (e unico) risultato della query COUNT(*)
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del numero totale di utenti: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

}