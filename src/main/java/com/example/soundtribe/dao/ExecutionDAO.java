package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Execution;
import com.example.soundtribe.entità.Instrument;
import java.sql.*;
import java.util.*;

public class ExecutionDAO {

    // LISTA STRUMENTI PREDEFINITI (Popolerà il dizionario al primo avvio)
    private static final List<String> PRESET_STRUMENTI = Arrays.asList(
            "Armonica", "Basso", "Batteria", "Chitarra acustica", "Chitarra classica",
            "Chitarra elettrica", "Contrabbasso", "Flauto dolce", "Flauto traverso",
            "Pianoforte", "Pianola", "Sax", "Violino", "Violoncello"
    );

    public ExecutionDAO() {
        initTable();
    }

    private void initTable() {
        // INGEGNERIA DEL SOFTWARE: La tabella base non contiene più la colonna "instruments" testuale
        String sqlMedia = "CREATE TABLE IF NOT EXISTS media_files (" +
                "id SERIAL PRIMARY KEY, " +
                "song_id INT, " +
                "title TEXT, " +
                "file_path TEXT NOT NULL, " +
                "file_type VARCHAR(50), " +
                "executors TEXT, " +
                "duration VARCHAR(20), " +
                "is_live BOOLEAN DEFAULT FALSE, " +
                "recording_date DATE, " +
                "recording_place TEXT, " +
                "is_concert BOOLEAN DEFAULT FALSE, " +
                "is_self_performer BOOLEAN DEFAULT FALSE, " +
                "uploader_id INT, " +
                "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE SET NULL" +
                ")";

        // 1. LA TABELLA DIZIONARIO PER GLI STRUMENTI (Primary Key)
        String sqlInstruments = "CREATE TABLE IF NOT EXISTS instruments (" +
                "name VARCHAR(100) PRIMARY KEY" +
                ")";

        // 2. TABELLA PONTE MOLTI-A-MOLTI (Esecuzione <-> Strumenti)
        String sqlExecInst = "CREATE TABLE IF NOT EXISTS execution_instruments (" +
                "execution_id INT, " +
                "instrument_name VARCHAR(100), " +
                "PRIMARY KEY (execution_id, instrument_name), " +
                "FOREIGN KEY (execution_id) REFERENCES media_files(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (instrument_name) REFERENCES instruments(name) ON DELETE CASCADE" +
                ")";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlMedia);
            stmt.execute(sqlInstruments);
            stmt.execute(sqlExecInst);

            // Popola il dizionario di base
            for (String inst : PRESET_STRUMENTI) {
                try {
                    stmt.execute("INSERT INTO instruments (name) VALUES ('" + inst.replace("'", "''") + "') ON CONFLICT DO NOTHING");
                } catch (SQLException ignore) {} // Ignora se esiste già
            }
        } catch (SQLException e) {
            System.err.println("Errore tabelle esecuzioni: " + e.getMessage());
        }
    }

    public void addMedia(Execution media) {
        // Query di inserimento corretta (12 parametri, senza la colonna instruments ridondante)
        String sql = "INSERT INTO media_files (song_id, title, file_path, file_type, executors, duration, is_live, recording_date, recording_place, is_concert, is_self_performer, uploader_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (media.getSongId() == 0) pstmt.setNull(1, Types.INTEGER);
            else pstmt.setInt(1, media.getSongId());

            pstmt.setString(2, media.getTitle());
            pstmt.setString(3, media.getFilePath());
            pstmt.setString(4, media.getFileType());
            pstmt.setString(5, media.getExecutors());
            pstmt.setString(6, media.getDuration());
            pstmt.setBoolean(7, media.isLive());
            pstmt.setDate(8, media.getRecordingDate());
            pstmt.setString(9, media.getRecordingPlace());
            pstmt.setBoolean(10, media.isConcert());
            pstmt.setBoolean(11, media.isSelfPerformer());
            pstmt.setInt(12, media.getUploaderId());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int newExecutionId = rs.getInt(1);

                // --- LOGICA DI SALVATAGGIO RELAZIONALE ---
                if (media.getInstruments() != null && !media.getInstruments().isEmpty()) {
                    String[] insts = media.getInstruments().split(",");
                    String insertDict = "INSERT INTO instruments (name) VALUES (?) ON CONFLICT DO NOTHING";
                    String insertRel = "INSERT INTO execution_instruments (execution_id, instrument_name) VALUES (?, ?)";

                    try (PreparedStatement psDict = conn.prepareStatement(insertDict);
                         PreparedStatement psRel = conn.prepareStatement(insertRel)) {

                        for (String inst : insts) {
                            String cleanInst = inst.trim();
                            if (cleanInst.isEmpty()) continue;

                            // 1. Assicurati che lo strumento esista nel dizionario master
                            psDict.setString(1, cleanInst);
                            psDict.executeUpdate();

                            // 2. Crea il collegamento nella tabella ponte
                            psRel.setInt(1, newExecutionId);
                            psRel.setString(2, cleanInst);
                            psRel.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore salvataggio media: " + e.getMessage());
        }
    }

    public List<Execution> getAllExecutions() {
        List<Execution> mediaList = new ArrayList<>();
        // Ingegneria del SW: Ricostruiamo la stringa degli strumenti "al volo" tramite STRING_AGG per passarla all'interfaccia UI
        String sql = "SELECT m.*, " +
                "(SELECT STRING_AGG(instrument_name, ', ') FROM execution_instruments WHERE execution_id = m.id) AS inst_list " +
                "FROM media_files m ORDER BY id DESC";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) mediaList.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return mediaList;
    }

    public List<Execution> getMediaBySongId(int songId) {
        List<Execution> mediaList = new ArrayList<>();
        String sql = "SELECT m.*, " +
                "(SELECT STRING_AGG(instrument_name, ', ') FROM execution_instruments WHERE execution_id = m.id) AS inst_list " +
                "FROM media_files m WHERE song_id = ?";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, songId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) mediaList.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mediaList;
    }

    public List<Execution> searchExecutions(String query) {
        List<Execution> results = new ArrayList<>();
        // Ricerca Globale: Cerca nel titolo, negli esecutori OPPURE direttamente tramite subquery nella tabella ponte degli strumenti
        String sql = "SELECT m.*, " +
                "(SELECT STRING_AGG(instrument_name, ', ') FROM execution_instruments WHERE execution_id = m.id) AS inst_list " +
                "FROM media_files m " +
                "WHERE m.title ILIKE ? OR m.executors ILIKE ? " +
                "OR EXISTS (SELECT 1 FROM execution_instruments ei WHERE ei.execution_id = m.id AND ei.instrument_name ILIKE ?)";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore ricerca esecuzioni: " + e.getMessage());
        }
        return results;
    }

    private Execution mapRow(ResultSet rs) throws SQLException {
        Execution e = new Execution(
                rs.getInt("id"),
                rs.getInt("song_id"),
                rs.getString("title"),
                rs.getString("file_path"),
                rs.getString("file_type"),
                rs.getString("executors"),
                null, // Lo valorizziamo sotto
                rs.getString("duration"),
                rs.getBoolean("is_live"),
                rs.getDate("recording_date"),
                rs.getString("recording_place"),
                rs.getBoolean("is_concert"),
                rs.getBoolean("is_self_performer"),
                rs.getInt("uploader_id")
        );

        // Estraiamo la lista degli strumenti aggregata dal database
        try {
            String instList = rs.getString("inst_list");
            e.setInstruments(instList != null ? instList : "");
        } catch (SQLException ex) {
            e.setInstruments(""); // Fallback sicuro
        }
        return e;
    }

    // IL VERO DIZIONARIO DEGLI STRUMENTI (Recupera dalla tabella dedicata)
    public List<Instrument> getAllInstruments() {
        List<Instrument> list = new ArrayList<>();
        String sql = "SELECT name FROM instruments ORDER BY name ASC";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Instrument(rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero strumenti dal DB: " + e.getMessage());
        }
        return list;
    }

    /**
     * Recupera l'elenco dei nomi di tutti gli strumenti presenti nel dizionario.
     * Restituisce una lista di Stringhe ideale per popolare ComboBox o menu a tendina.
     */
    public List<String> getDistinctInstruments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM instruments ORDER BY name ASC";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero nomi strumenti dal DB: " + e.getMessage());
        }

        return list;
    }
}