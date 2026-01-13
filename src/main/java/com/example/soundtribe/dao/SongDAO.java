package com.example.soundtribe.dao;

import com.example.soundtribe.Song;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {
    private String dbUrl;

    public SongDAO() {

            this.dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";

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
                "genre TEXT, " +
                "pdf_sheet_path TEXT, " +
                "audio_path TEXT, " +
                "youtube_url TEXT" +
                ")";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Migration for existing table without new columns
            try {
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS pdf_sheet_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS audio_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS youtube_url TEXT");
            } catch (SQLException ignore) {}

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione/aggiornamento della tabella: " + e.getMessage());
        }
    }

    private Song mapRow(ResultSet rs) throws SQLException {
        return new Song(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("genre"),
                rs.getString("pdf_sheet_path"),
                rs.getString("audio_path"),
                rs.getString("youtube_url")
        );
    }

    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(mapRow(rs));
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
                    songs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca dei brani: " + e.getMessage());
        }
        return songs;
    }

    public void addSong(Song song) {
        String sql = "INSERT INTO songs (title, artist, genre, pdf_sheet_path, audio_path, youtube_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getGenre());
            pstmt.setString(4, song.getPdfSheetPath());
            pstmt.setString(5, song.getAudioPath());
            pstmt.setString(6, song.getYoutubeUrl());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiunta del brano: " + e.getMessage());
        }
    }
}
