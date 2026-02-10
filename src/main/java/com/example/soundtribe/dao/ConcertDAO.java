package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Concert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcertDAO {
    private String dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";
    private String user = "postgres";
    private String password = "AppSoundtribe14";

    public ConcertDAO() {
        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
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
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addConcert(Concert concert) {
        String sql = "INSERT INTO concerts (title, artist, youtube_url, concert_date, location, description, uploader_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, concert.getTitle());
            pstmt.setString(2, concert.getArtist());
            pstmt.setString(3, concert.getYoutubeUrl());
            pstmt.setDate(4, concert.getDate());
            pstmt.setString(5, concert.getLocation());
            pstmt.setString(6, concert.getDescription());
            pstmt.setInt(7, concert.getUploaderId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Concert> getAllConcerts() {
        List<Concert> list = new ArrayList<>();
        String sql = "SELECT * FROM concerts ORDER BY id DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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