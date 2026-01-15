package com.example.soundtribe.dao;

import com.example.soundtribe.entità.User;
import java.sql.*;

public class UserDAO {
    private String dbUrl;
    private String user;
    private String password;

    public UserDAO() {
        // Credenziali del tuo database PostgreSQL
        this.dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";
        this.user = "postgres";
        this.password = "AppSoundtribe14";

        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
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
                "profile_pic_path TEXT, " +     // Nuova colonna
                "favorite_genre TEXT" +         // Nuova colonna
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Crea la tabella base se non c'è
            stmt.execute(sql);

            // 2. Migrazione: Se la tabella esisteva già, aggiungiamo le colonne nuove
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_pic_path TEXT");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS favorite_genre TEXT");
            } catch (SQLException ignore) {
                // Ignoriamo errori se le colonne esistono già
            }

        } catch (SQLException e) {
            System.err.println("Errore creazione/aggiornamento tabella users: " + e.getMessage());
        }
    }

    // Metodo per Registrare un nuovo utente (INSERT)
    public boolean registerUser(User user) {
        // Nota: Al momento della registrazione profile_pic e genere sono NULL, va bene così.
        String sql = "INSERT INTO users (name, surname, email, password, is_admin) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getSurname());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setBoolean(5, user.isAdmin());

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

    // Metodo per aggiornare i dati di un utente esistente (UPDATE)
    // Usato da ProfileController
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, password = ?, profile_pic_path = ?, favorite_genre = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getProfilePicPath());
            pstmt.setString(4, user.getFavoriteGenre());
            pstmt.setInt(5, user.getId()); // WHERE id = ?

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento del profilo: " + e.getMessage());
            return false;
        }
    }

    // Metodo per il Login
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = getConnection();
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

    // Metodo per recuperare un utente dato il suo ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
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

    // Mappatura da ResultSet a Oggetto User
    private User mapRow(ResultSet rs) throws SQLException {
        // Creiamo l'utente con il costruttore base
        User user = new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("surname"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getBoolean("is_admin")
        );

        // Aggiungiamo i campi opzionali (che potrebbero essere NULL nel database)
        user.setProfilePicPath(rs.getString("profile_pic_path"));
        user.setFavoriteGenre(rs.getString("favorite_genre"));

        return user;
    }
}