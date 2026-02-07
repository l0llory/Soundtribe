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
        // Creazione tabella media_files collegata a songs
        String sql = "CREATE TABLE IF NOT EXISTS media_files (" +
                "id SERIAL PRIMARY KEY, " +
                "song_id INT NOT NULL, " +
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
                "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE" +
                ")";
        // ON DELETE CASCADE: Se cancelli la canzone, si cancellano anche i file associati

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Errore creazione tabella media_files: " + e.getMessage());
        }
    }

    // Aggiungi un nuovo file multimediale
    public void addMedia(Esecution media) {
        String sql = "INSERT INTO media_files (song_id, file_path, file_type, executors, instruments, duration, is_live, recording_date, recording_place, is_concert, is_self_performer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, media.getSongId());
            pstmt.setString(2, media.getFilePath());
            pstmt.setString(3, media.getFileType());
            pstmt.setString(4, media.getExecutors());
            pstmt.setString(5, media.getInstruments());
            pstmt.setString(6, media.getDuration());
            pstmt.setBoolean(7, media.isLive());
            pstmt.setDate(8, media.getRecordingDate()); // java.sql.Date
            pstmt.setString(9, media.getRecordingPlace());
            pstmt.setBoolean(10, media.isConcert());
            pstmt.setBoolean(11, media.isSelfPerformer());

            pstmt.executeUpdate();
            System.out.println("Media salvato correttamente per song_id: " + media.getSongId());

        } catch (SQLException e) {
            System.err.println("Errore salvataggio media: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Recupera tutti i file associati a una specifica canzone
    public List<Esecution> getMediaBySongId(int songId) {
        List<Esecution> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media_files WHERE song_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, songId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mediaList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero media: " + e.getMessage());
        }
        return mediaList;
    }

    private Esecution mapRow(ResultSet rs) throws SQLException {
        return new Esecution(
                rs.getInt("id"),
                rs.getInt("song_id"),
                rs.getString("file_path"),
                rs.getString("file_type"),
                rs.getString("executors"),
                rs.getString("instruments"),
                rs.getString("duration"),
                rs.getBoolean("is_live"),
                rs.getDate("recording_date"),
                rs.getString("recording_place"),
                rs.getBoolean("is_concert"),
                rs.getBoolean("is_self_performer")
        );
    }
}