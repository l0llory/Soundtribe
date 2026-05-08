package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Esecution;
import com.example.soundtribe.entità.Instrument;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class EsecutionDAO {

    // LISTA STRUMENTI PREDEFINITI
    private static final List<String> PRESET_STRUMENTI = Arrays.asList(
            "Armonica",
            "Basso",
            "Batteria",
            "Chitarra acustica",
            "Chitarra classica",
            "Chitarra elettrica",
            "Contrabbasso",
            "Flauto dolce",
            "Flauto traverso",
            "Pianoforte",
            "Pianola",
            "Sax",
            "Violino",
            "Violoncello"
    );

    public EsecutionDAO() {
        initTable();
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

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

            try {
                stmt.execute("ALTER TABLE media_files ADD COLUMN IF NOT EXISTS title TEXT");
                stmt.execute("ALTER TABLE media_files ADD COLUMN IF NOT EXISTS uploader_id INT");
                stmt.execute("ALTER TABLE media_files ALTER COLUMN song_id DROP NOT NULL");
            } catch (SQLException ignore) {}

        } catch (SQLException e) {
            System.err.println("Errore tabella media_files: " + e.getMessage());
        }
    }

    public void addMedia(Esecution media) {
        String sql = "INSERT INTO media_files (song_id, title, file_path, file_type, executors, instruments, duration, is_live, recording_date, recording_place, is_concert, is_self_performer, uploader_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CredDAO.getConnection();
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
        try (Connection conn = CredDAO.getConnection();
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
        try (Connection conn = CredDAO.getConnection();
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
                rs.getString("title"),
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
        String sql = "SELECT * FROM media_files WHERE title ILIKE ? OR executors ILIKE ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca esecuzioni: " + e.getMessage());
        }
        return results;
    }

    // --- GESTIONE DIZIONARIO STRUMENTI ---

    // Metodo per ottenere la lista di stringhe (usato da alcune parti legacy o per debug)
    public List<String> getDistinctInstruments() {
        // Usiamo getAllInstruments per logica unificata e poi estraiamo i nomi
        return getAllInstruments().stream()
                .map(Instrument::getName)
                .collect(Collectors.toList());
    }

    /**
     * Recupera tutti gli strumenti disponibili.
     * Unisce la lista PRESET (statica) con quella presente nel Database.
     * @return Lista ordinata di oggetti Instrument
     */
    public List<Instrument> getAllInstruments() {
        // 1. Usiamo un TreeSet con un comparatore Case-Insensitive per evitare duplicati e ordinare
        Set<String> uniqueInstruments = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // 2. Aggiungi i PRESET
        uniqueInstruments.addAll(PRESET_STRUMENTI);

        // 3. Aggiungi quelli esistenti nel DB (aggiunti dagli utenti)
        String sql = "SELECT DISTINCT instruments FROM media_files WHERE instruments IS NOT NULL AND instruments <> ''";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String dbInst = rs.getString("instruments");
                // Separiamo eventuali liste separate da virgola nel DB per pulizia
                if (dbInst.contains(",")) {
                    String[] parts = dbInst.split(",");
                    for (String part : parts) {
                        uniqueInstruments.add(part.trim());
                    }
                } else {
                    uniqueInstruments.add(dbInst.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero strumenti dal DB: " + e.getMessage());
        }

        // 4. Converti il Set di stringhe in una List di oggetti Instrument
        return uniqueInstruments.stream()
                .map(Instrument::new)
                .collect(Collectors.toList());
    }
}