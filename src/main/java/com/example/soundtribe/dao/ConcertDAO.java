package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Concert;
import com.example.soundtribe.entità.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcertDAO {

    public ConcertDAO() {
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS concerts (" +
                "id SERIAL PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "artist TEXT, " +
                "youtube_url TEXT NOT NULL, " +
                "concert_date DATE, " +
                "location TEXT, " +
                "description TEXT, " +
                "uploader_id INT" +
                ")";

        String sqlTracks = "CREATE TABLE IF NOT EXISTS concert_tracks (" +
                "id SERIAL PRIMARY KEY, " +
                "concert_id INT, " +
                "title TEXT, " +
                "executors TEXT, " +
                "start_time VARCHAR(10), " +
                "end_time VARCHAR(10), " +
                "FOREIGN KEY (concert_id) REFERENCES concerts(id) ON DELETE CASCADE" +
                ")";

        String sqlTrackInst = "CREATE TABLE IF NOT EXISTS track_instruments (" +
                "track_id INT, " +
                "instrument_name VARCHAR(100), " +
                "PRIMARY KEY (track_id, instrument_name), " +
                "FOREIGN KEY (track_id) REFERENCES concert_tracks(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (instrument_name) REFERENCES instruments(name) ON DELETE CASCADE" +
                ")";

        try (Connection conn = CredDAO.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sqlTracks);
            stmt.execute(sqlTrackInst);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addConcert(Concert concert) {
        // 1. INSERIAMO IL CONCERTO E RECUPERIAMO IL SUO ID GENERATO
        String sqlConcert = "INSERT INTO concerts (title, artist, youtube_url, concert_date, location, description, uploader_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlConcert)) {

            pstmt.setString(1, concert.getTitle());
            pstmt.setString(2, concert.getArtist());
            pstmt.setString(3, concert.getYoutubeUrl());
            pstmt.setDate(4, concert.getDate());
            pstmt.setString(5, concert.getLocation());
            pstmt.setString(6, concert.getDescription());
            pstmt.setInt(7, concert.getUploaderId());

            ResultSet rsConcert = pstmt.executeQuery();
            if (rsConcert.next()) {
                int newConcertId = rsConcert.getInt(1);

                // 2. SALVIAMO OGNI TRACCIA DELLA SCALETTA E RECUPERIAMO IL SUO ID
                if (concert.getTracks() != null && !concert.getTracks().isEmpty()) {
                    String sqlTrack = "INSERT INTO concert_tracks (concert_id, title, executors, start_time, end_time) VALUES (?, ?, ?, ?, ?) RETURNING id";

                    try (PreparedStatement pstmtTrack = conn.prepareStatement(sqlTrack)) {
                        for (ConcertTrack track : concert.getTracks()) {
                            pstmtTrack.setInt(1, newConcertId);
                            pstmtTrack.setString(2, track.getTitle());
                            pstmtTrack.setString(3, track.getExecutors());
                            pstmtTrack.setString(4, track.getStartTime());
                            pstmtTrack.setString(5, track.getEndTime());

                            ResultSet rsTrack = pstmtTrack.executeQuery();
                            if (rsTrack.next()) {
                                int newTrackId = rsTrack.getInt(1);

                                // 3. SALVIAMO GLI STRUMENTI NEL DIZIONARIO E NELLA TABELLA PONTE
                                if (track.getInstruments() != null && !track.getInstruments().isEmpty()) {
                                    String[] insts = track.getInstruments().split(",");
                                    String insertDict = "INSERT INTO instruments (name) VALUES (?) ON CONFLICT DO NOTHING";
                                    String insertRel = "INSERT INTO track_instruments (track_id, instrument_name) VALUES (?, ?)";

                                    try (PreparedStatement psDict = conn.prepareStatement(insertDict);
                                         PreparedStatement psRel = conn.prepareStatement(insertRel)) {

                                        for (String inst : insts) {
                                            String cleanInst = inst.trim();
                                            if (cleanInst.isEmpty()) continue;

                                            // Assicura che lo strumento esista nel dizionario
                                            psDict.setString(1, cleanInst);
                                            psDict.executeUpdate();

                                            // Crea la relazione tra questa specifica traccia e lo strumento
                                            psRel.setInt(1, newTrackId);
                                            psRel.setString(2, cleanInst);
                                            psRel.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Concert> getAllConcerts() {
        List<Concert> list = new ArrayList<>();
        String sql = "SELECT * FROM concerts ORDER BY id DESC";
        try (Connection conn = CredDAO.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Concert(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("youtube_url"),
                        rs.getDate("concert_date"),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getInt("uploader_id")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}