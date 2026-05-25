package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Execution;
import com.example.soundtribe.entità.ExecutionSegment;
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

        String sqlInstruments = "CREATE TABLE IF NOT EXISTS instruments (" +
                "name VARCHAR(100) PRIMARY KEY" +
                ")";

        String sqlExecInst = "CREATE TABLE IF NOT EXISTS execution_instruments (" +
                "execution_id INT, " +
                "instrument_name VARCHAR(100), " +
                "PRIMARY KEY (execution_id, instrument_name), " +
                "FOREIGN KEY (execution_id) REFERENCES media_files(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (instrument_name) REFERENCES instruments(name) ON DELETE CASCADE" +
                ")";

        // NUOVA TABELLA PER I SEGMENTI DELLE ESECUZIONI
        String sqlSegments = "CREATE TABLE IF NOT EXISTS execution_segments (" +
                "id SERIAL PRIMARY KEY, " +
                "execution_id INT, " +
                "user_id INT, " +
                "start_time VARCHAR(10), " +
                "end_time VARCHAR(10), " +
                "notes TEXT, " +
                "FOREIGN KEY (execution_id) REFERENCES media_files(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlMedia);
            stmt.execute(sqlInstruments);
            stmt.execute(sqlExecInst);
            stmt.execute(sqlSegments); // Creazione tabella segmenti

            // Popola il dizionario di base
            for (String inst : PRESET_STRUMENTI) {
                try {
                    stmt.execute("INSERT INTO instruments (name) VALUES ('" + inst.replace("'", "''") + "') ON CONFLICT DO NOTHING");
                } catch (SQLException ignore) {}
            }
        } catch (SQLException e) {
            System.err.println("Errore tabelle esecuzioni: " + e.getMessage());
        }
    }

    public void addMedia(Execution media) {
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

                if (media.getInstruments() != null && !media.getInstruments().isEmpty()) {
                    String[] insts = media.getInstruments().split(",");
                    String insertDict = "INSERT INTO instruments (name) VALUES (?) ON CONFLICT DO NOTHING";
                    String insertRel = "INSERT INTO execution_instruments (execution_id, instrument_name) VALUES (?, ?)";

                    try (PreparedStatement psDict = conn.prepareStatement(insertDict);
                         PreparedStatement psRel = conn.prepareStatement(insertRel)) {

                        for (String inst : insts) {
                            String cleanInst = inst.trim();
                            if (cleanInst.isEmpty()) continue;
                            psDict.setString(1, cleanInst);
                            psDict.executeUpdate();
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
                null,
                rs.getString("duration"),
                rs.getBoolean("is_live"),
                rs.getDate("recording_date"),
                rs.getString("recording_place"),
                rs.getBoolean("is_concert"),
                rs.getBoolean("is_self_performer"),
                rs.getInt("uploader_id")
        );

        try {
            String instList = rs.getString("inst_list");
            e.setInstruments(instList != null ? instList : "");
        } catch (SQLException ex) {
            e.setInstruments("");
        }
        return e;
    }

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

    // --- NUOVI METODI PER LA GESTIONE DEI SEGMENTI ANNOTATI ---

    public void addSegmentToExecution(ExecutionSegment segment) {
        String sql = "INSERT INTO execution_segments (execution_id, user_id, start_time, end_time, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, segment.getExecutionId());
            pstmt.setInt(2, segment.getUserId());
            pstmt.setString(3, segment.getStartTime());
            pstmt.setString(4, segment.getEndTime());
            pstmt.setString(5, segment.getNotes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore inserimento segmento esecuzione: " + e.getMessage());
        }
    }

    public List<ExecutionSegment> getSegmentsForExecution(int executionId) {
        List<ExecutionSegment> segments = new ArrayList<>();
        String sql = "SELECT * FROM execution_segments WHERE execution_id = ? ORDER BY start_time ASC";
        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, executionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExecutionSegment seg = new ExecutionSegment();
                    seg.setId(rs.getInt("id"));
                    seg.setExecutionId(rs.getInt("execution_id"));
                    seg.setUserId(rs.getInt("user_id"));
                    seg.setStartTime(rs.getString("start_time"));
                    seg.setEndTime(rs.getString("end_time"));
                    seg.setNotes(rs.getString("notes"));
                    segments.add(seg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return segments;
    }
}