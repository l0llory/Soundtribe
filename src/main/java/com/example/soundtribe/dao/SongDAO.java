package com.example.soundtribe.dao;

import com.example.soundtribe.entita.Song;

import java.sql.*;
import java.util.*;

public class SongDAO {

    // DIZIONARIO MASTER: Lista di generi predefiniti nel sistema
    private static final List<String> PRESET_GENERI = Arrays.asList(
            "Afro", "Blues", "Folk", "Indie", "Jazz", "Musica classica",
            "Pop", "Reggae", "Rap", "Reggaeton", "Rock", "Trap"
    );

    public SongDAO() {
        initTable();
    }

    private void initTable() {
        // INGEGNERIA DEL SOFTWARE: Schema normalizzato senza ridondanze e con Chiave Esterna (FK)
        String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                "id SERIAL PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "artist TEXT NOT NULL, " +
                "genre TEXT, " +
                "pdf_sheet_path TEXT, " +
                "audio_path TEXT, " +
                "youtube_url TEXT, " +
                "cover_path TEXT, " +
                "uploader_id INT, " +
                "description TEXT, " +
                "FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE SET NULL" +
                ")";
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Migrazione colonne per database preesistenti
            try {
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS pdf_sheet_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS audio_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS youtube_url TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS cover_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS uploader_id INT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS description TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW()");
            } catch (SQLException ignore) {}

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione/aggiornamento della tabella: " + e.getMessage());
        }
    }

    /**
     * Mappa i risultati del ResultSet valorizzando opzionalmente nome e cognome
     * estratti dinamicamente dalla JOIN con la tabella users.
     */
    private Song mapRow(ResultSet rs) throws SQLException {
        String uploaderName = null;
        String uploaderSurname = null;

        try {
            uploaderName = rs.getString("uploader_name");
            uploaderSurname = rs.getString("uploader_surname");
        } catch (SQLException e) {
            // Le colonne della JOIN non sono presenti se viene invocata una query secca
        }

        return new Song(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("genre"),
                rs.getString("pdf_sheet_path"),
                rs.getString("audio_path"),
                rs.getString("youtube_url"),
                rs.getString("cover_path"),
                rs.getInt("uploader_id"),
                uploaderName,
                uploaderSurname,
                rs.getString("description")
        );
    }

    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        // Query ottimizzata con JOIN relazionale per raccogliere i dati anagrafici dell'uploader
        String sql = "SELECT s.*, u.name AS uploader_name, u.surname AS uploader_surname " +
                "FROM songs s " +
                "LEFT JOIN users u ON s.uploader_id = u.id " +
                "ORDER BY s.id";
        try (Connection conn = CredDAO.getConnection();
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
        String sql = "SELECT s.*, u.name AS uploader_name, u.surname AS uploader_surname " +
                "FROM songs s " +
                "LEFT JOIN users u ON s.uploader_id = u.id " +
                "WHERE s.title ILIKE ? OR s.artist ILIKE ?";
        try (Connection conn = CredDAO.getConnection();
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
        // Query di inserimento corretta senza le colonne ridondanti uploader_name e uploader_surname
        String sql = "INSERT INTO songs (title, artist, genre, pdf_sheet_path, audio_path, youtube_url, cover_path, uploader_id, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getGenre());
            pstmt.setString(4, song.getPdfSheetPath());
            pstmt.setString(5, song.getAudioPath());
            pstmt.setString(6, song.getYoutubeUrl());
            pstmt.setString(7, song.getCoverPath());

            if (song.getUploader_id() > 0) {
                pstmt.setInt(8, song.getUploader_id());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            pstmt.setString(9, song.getDescription());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiunta del brano: " + e.getMessage());
        }
    }

    public Song getSongById(int songId) {
        Song song = null;
        String sql = "SELECT s.*, u.name AS uploader_name, u.surname AS uploader_surname " +
                "FROM songs s " +
                "LEFT JOIN users u ON s.uploader_id = u.id " +
                "WHERE s.id = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, songId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    song = mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del brano per ID: " + e.getMessage());
            e.printStackTrace();
        }
        return song;
    }

    public List<Song> getSongsCommentedByUser(int userId) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT DISTINCT s.*, u.name AS uploader_name, u.surname AS uploader_surname " +
                "FROM songs s " +
                "JOIN comments c ON s.id = c.song_id " +
                "LEFT JOIN users u ON s.uploader_id = u.id " +
                "WHERE c.user_id = ?";

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero brani commentati: " + e.getMessage());
        }
        return songs;
    }

    public List<String> getDistinctAuthors() {
        List<String> authors = new ArrayList<>();
        String sql = "SELECT DISTINCT artist FROM songs WHERE artist IS NOT NULL AND artist <> '' ORDER BY artist";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                authors.add(rs.getString("artist"));
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero autori: " + e.getMessage());
        }
        return authors;
    }

    public List<String> getDistinctGenres() {
        Set<String> uniqueGenres = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        uniqueGenres.addAll(PRESET_GENERI);

        String sql = "SELECT DISTINCT genre FROM songs WHERE genre IS NOT NULL AND genre <> ''";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                uniqueGenres.add(rs.getString("genre").trim());
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero generi dal DB: " + e.getMessage());
        }

        return new ArrayList<>(uniqueGenres);
    }

    public List<Song> searchSongsAdvanced(String textQuery, String author, String genre) {
        List<Song> songs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, u.name AS uploader_name, u.surname AS uploader_surname " +
                "FROM songs s " +
                "LEFT JOIN users u ON s.uploader_id= u.id " +
                "WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (textQuery != null && !textQuery.trim().isEmpty()) {
            sql.append(" AND (s.title ILIKE ? OR s.artist ILIKE ?)");
            parameters.add("%" + textQuery.trim() + "%");
            parameters.add("%" + textQuery.trim() + "%");
        }

        if (author != null && !author.trim().isEmpty() && !author.equals("Tutti")) {
            sql.append(" AND s.artist = ?");
            parameters.add(author);
        }

        if (genre != null && !genre.trim().isEmpty() && !genre.equals("Tutti")) {
            sql.append(" AND s.genre ILIKE ?");
            parameters.add(genre);
        }

        try (Connection conn = CredDAO.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca avanzata dei brani: " + e.getMessage());
        }
        return songs;
    }

    public List<String> getDistinctTitles() {
        List<String> titles = new ArrayList<>();
        String sql = "SELECT DISTINCT title FROM songs WHERE title IS NOT NULL AND title <> '' ORDER BY title";

        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                titles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero titoli: " + e.getMessage());
        }
        return titles;
    }
}