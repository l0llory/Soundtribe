package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Esecution;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EsecutionDAO {
    private String dbUrl;
    private String user;
    private String password;

    public EsecutionDAO() {
        this.dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";
        this.user = "postgres";
        this.password = "AppSoundtribe14";
        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS media_files (" +
                "id SERIAL PRIMARY KEY, " +
                "song_id INT, " +
                "title TEXT, " +
                "file_path TEXT NOT NULL, " +
                "file_type VARCHAR(50), " +
                "executors TEXT, " +
                "instruments TEXT, " +
                "duration VARCHAR(20), " +
                "is_live BOOLEAN DEFAULT FALSE, " +
                "recording_date DATE, " +
                "recording_place TEXT, " +
                "is_concert BOOLEAN DEFAULT FALSE, " +
                "is_self_performer BOOLEAN DEFAULT FALSE, " +
                "uploader_id INT, " +
                "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE SET NULL" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Crea la tabella se non esiste
            stmt.execute(sql);

            // 2. MIGRAZIONE COLONNE (Se mancano, le aggiunge)
            try {
                stmt.execute("ALTER TABLE media_files ADD COLUMN IF NOT EXISTS title TEXT");
                stmt.execute("ALTER TABLE media_files ADD COLUMN IF NOT EXISTS uploader_id INT");
            } catch (SQLException ignore) {}

            // 3. MIGRAZIONE CONSTRAINT (SOLUZIONE AL TUO ERRORE)
            // Rimuove l'obbligo di NOT NULL su song_id se presente
            try {
                stmt.execute("ALTER TABLE media_files ALTER COLUMN song_id DROP NOT NULL");
            } catch (SQLException e) {
                System.out.println("Nota: Impossibile rimuovere NOT NULL da song_id (forse è già nullable): " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Errore tabella media_files: " + e.getMessage());
        }
    }

    public void addMedia(Esecution media) {
        // Aggiunto il campo 'title' alla query
        String sql = "INSERT INTO media_files (song_id, title, file_path, file_type, executors, instruments, duration, is_live, recording_date, recording_place, is_concert, is_self_performer, uploader_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (media.getSongId() == 0) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, media.getSongId());
            }

            pstmt.setString(2, media.getTitle());
            pstmt.setString(3, media.getFilePath());
            pstmt.setString(4, media.getFileType());
            pstmt.setString(5, media.getExecutors());
            pstmt.setString(6, media.getInstruments());
            pstmt.setString(7, media.getDuration());
            pstmt.setBoolean(8, media.isLive());
            pstmt.setDate(9, media.getRecordingDate());
            pstmt.setString(10, media.getRecordingPlace());
            pstmt.setBoolean(11, media.isConcert());
            pstmt.setBoolean(12, media.isSelfPerformer());
            pstmt.setInt(13, media.getUploaderId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore salvataggio media: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Esecution> getMediaBySongId(int songId) {
        List<Esecution> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media_files WHERE song_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, songId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) mediaList.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mediaList;
    }

    public List<Esecution> getAllExecutions() {
        List<Esecution> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media_files ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) mediaList.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return mediaList;
    }

    private Esecution mapRow(ResultSet rs) throws SQLException {
        return new Esecution(
                rs.getInt("id"),
                rs.getInt("song_id"),
                rs.getString("title"), // <--- LEGGI TITOLO
                rs.getString("file_path"),
                rs.getString("file_type"),
                rs.getString("executors"),
                rs.getString("instruments"),
                rs.getString("duration"),
                rs.getBoolean("is_live"),
                rs.getDate("recording_date"),
                rs.getString("recording_place"),
                rs.getBoolean("is_concert"),
                rs.getBoolean("is_self_performer"),
                rs.getInt("uploader_id")
        );
    }

    public List<Esecution> searchExecutions(String query) {
        List<Esecution> results = new ArrayList<>();
        // Cerca nel titolo oppure, opzionalmente, negli esecutori
        String sql = "SELECT * FROM media_files WHERE title ILIKE ? OR executors ILIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca esecuzioni: " + e.getMessage());
        }
        return results;
    }
}