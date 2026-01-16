package com.example.soundtribe.dao;

import com.example.soundtribe.entità.Song;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {
    private String dbUrl;
    private String user;
    private String password;

    public SongDAO() {
        // Assicurati che le credenziali siano corrette per il tuo DB locale
        this.dbUrl = "jdbc:postgresql://localhost:5432/soundtribe";
        this.user = "postgres";
        this.password = "AppSoundtribe14";

        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    private void initTable() {
        // Creazione tabella base con TUTTI i nuovi campi
        String sql = "CREATE TABLE IF NOT EXISTS songs (" +
                "id SERIAL PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "artist TEXT NOT NULL, " +
                "genre TEXT, " +
                "pdf_sheet_path TEXT, " +
                "audio_path TEXT, " +
                "youtube_url TEXT, " +
                "cover_path TEXT, " +
                "uploaded_by INT, " +          // ID utente
                "uploader_name TEXT, " +       // Nome utente (cache)
                "uploader_surname TEXT, " +    // Cognome utente (cache)
                "description TEXT" +           // Descrizione brano
                ")";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Migrazione: se la tabella esisteva già senza colonne specifiche, le aggiunge
            try {
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS pdf_sheet_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS audio_path TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS youtube_url TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS cover_path TEXT");

                // Nuove colonne per utente e descrizione
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS uploaded_by INT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS uploader_name TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS uploader_surname TEXT");
                stmt.execute("ALTER TABLE songs ADD COLUMN IF NOT EXISTS description TEXT");
            } catch (SQLException ignore) {
                // Ignoriamo errori se le colonne esistono già
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione/aggiornamento della tabella: " + e.getMessage());
        }
    }

    private Song mapRow(ResultSet rs) throws SQLException {
        // Mappiamo tutti i campi, inclusi i nuovi, nel costruttore aggiornato di Song
        return new Song(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("genre"),
                rs.getString("pdf_sheet_path"),
                rs.getString("audio_path"),
                rs.getString("youtube_url"),
                rs.getString("cover_path"),
                rs.getInt("uploaded_by"),
                rs.getString("uploader_name"),
                rs.getString("uploader_surname"),
                rs.getString("description")
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
        // Query aggiornata con 11 parametri
        String sql = "INSERT INTO songs (title, artist, genre, pdf_sheet_path, audio_path, youtube_url, cover_path, uploaded_by, uploader_name, uploader_surname, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Dati base
            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getGenre());
            pstmt.setString(4, song.getPdfSheetPath());
            pstmt.setString(5, song.getAudioPath());
            pstmt.setString(6, song.getYoutubeUrl());
            pstmt.setString(7, song.getCoverPath());

            // Nuovi dati (Utente e Descrizione)
            if (song.getUploadedBy() > 0) {
                pstmt.setInt(8, song.getUploadedBy());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            pstmt.setString(9, song.getUploaderName());
            pstmt.setString(10, song.getUploaderSurname());
            pstmt.setString(11, song.getDescription());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiunta del brano: " + e.getMessage());
        }
    }
}