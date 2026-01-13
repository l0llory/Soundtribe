package com.example.soundtribe.dao;

import com.example.soundtribe.Song;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {
    private String dbUrl;

    public SongDAO() {
        // Replit database environment variable
        this.dbUrl = System.getenv("DATABASE_URL");
        if (this.dbUrl == null) {
            // Alternative if DATABASE_URL is not found
            this.dbUrl = System.getenv("PGURL");
        }
        if (this.dbUrl == null) {
            // Fallback for local development
            this.dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";
        }
        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                "id SERIAL PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "artist TEXT NOT NULL, " +
                "genre TEXT" +
                ")";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Errore durante la creazione della tabella: " + e.getMessage());
        }
    }

    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                songs.add(new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei brani: " + e.getMessage());
        }
        return songs;
    }

    public List<Song> searchSongs(String query) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE title ILIKE ? OR artist ILIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(new Song(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getString("genre")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca dei brani: " + e.getMessage());
        }
        return songs;
    }

    public void addSong(Song song) {
        String sql = "INSERT INTO songs (title, artist, genre) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getGenre());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiunta del brano: " + e.getMessage());
        }
    }
}

