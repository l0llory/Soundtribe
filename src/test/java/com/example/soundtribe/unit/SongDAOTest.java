package com.example.soundtribe.unit;

import com.example.soundtribe.TestDataSeeder;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("Test SongDAO - Ricerca Avanzata e Filtri")
public class SongDAOTest {

    private SongDAO songDAO;
    private UserDAO userDAO;
    private int testUserId;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        songDAO = new SongDAO();
        userDAO = new UserDAO();
        CommentDAO commentDAO = new CommentDAO();

        int[] userIds = TestDataSeeder.seed(userDAO, songDAO, commentDAO);
        testUserId = userIds[0];
    }

    private static void resetDatabase() {
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE comment_likes, comments, songs, users RESTART IDENTITY CASCADE");
            stmt.execute("INSERT INTO users (name, surname, email, password, is_admin, status) " +
                         "VALUES ('Admin', 'SoundTribe', 'admin@soundtribe.it', 'admin', TRUE, 'Verified')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Aggiunta di un brano")
    public void testAddSong() {
        Song newSong = new Song(
                0, "La cura", "Franco Battiato", "Pop",
                "", "", "", "",
                testUserId, "Luca", "Ricci", "Uno dei brani più belli di Battiato"
        );

        songDAO.addSong(newSong);

        List<Song> all = songDAO.getAllSongs();
        assertFalse(all.isEmpty());

        Song added = all.get(all.size() - 1);
        assertEquals("La cura", added.getTitle());
        assertEquals("Pop", added.getGenre());
        assertEquals(testUserId, added.getUploader_id(), "Il brano deve essere legato all'utente che lo ha caricato");
    }

    @Test
    @DisplayName("Recupero di tutti i brani")
    public void testGetAllSongs() {
        Song s1 = new Song(0, "Azzurro", "Adriano Celentano", "Pop", "", "", "", "", testUserId, "Luca", "Ricci", "");
        Song s2 = new Song(0, "Take Five", "Dave Brubeck", "Jazz", "", "", "", "", testUserId, "Luca", "Ricci", "");

        songDAO.addSong(s1);
        songDAO.addSong(s2);

        List<Song> all = songDAO.getAllSongs();
        assertNotNull(all);
        assertTrue(all.size() >= 12);
        for (Song s : all) {
            assertTrue(s.getUploader_id() >= 0, "uploader_id deve essere un valore valido");
        }
    }

    @Test
    @DisplayName("Ricerca brani per titolo")
    public void testSearchSongsByTitle() {
        Song song = new Song(
                0, "Bohemian Rhapsody", "Queen", "Rock",
                "", "", "", "",
                testUserId, "Luca", "Ricci", ""
        );
        songDAO.addSong(song);

        List<Song> results = songDAO.searchSongs("Bohemian");
        assertFalse(results.isEmpty(), "Dovrebbe trovare il brano cercato");
        assertEquals("Bohemian Rhapsody", results.get(0).getTitle());
    }

    @Test
    @DisplayName("Ricerca brani per artista")
    public void testSearchSongsByArtist() {
        Song song = new Song(
                0, "Imagine", "John Lennon", "Rock",
                "", "", "", "",
                testUserId, "Luca", "Ricci", ""
        );
        songDAO.addSong(song);

        List<Song> results = songDAO.searchSongs("Lennon");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> "John Lennon".equals(s.getArtist())));
    }

    @Test
    @DisplayName("Ricerca che non trova risultati")
    public void testSearchNoResults() {
        List<Song> results = songDAO.searchSongs("xkqzwmnotexist99");
        assertTrue(results.isEmpty(), "La ricerca non dovrebbe trovare nulla");
    }

    @Test
    @DisplayName("Ricerca avanzata filtrata per genere")
    public void testSearchAdvancedByGenre() {
        Song rock = new Song(0, "Whole Lotta Love", "Led Zeppelin", "Rock", "", "", "", "", testUserId, "Luca", "Ricci", "");
        Song jazz = new Song(0, "So What", "Miles Davis", "Jazz", "", "", "", "", testUserId, "Luca", "Ricci", "");

        songDAO.addSong(rock);
        songDAO.addSong(jazz);

        List<Song> rockResults = songDAO.searchSongsAdvanced("", null, "Rock");
        assertTrue(rockResults.stream().allMatch(s -> "Rock".equals(s.getGenre())), "Tutti i risultati dovrebbero essere Rock");
    }

    @Test
    @DisplayName("Ricerca avanzata con testo e genere combinati")
    public void testSearchAdvancedTextAndGenre() {
        Song song = new Song(0, "Estate", "Bruno Martino", "Jazz", "", "", "", "", testUserId, "Luca", "Ricci", "");
        songDAO.addSong(song);

        List<Song> results = songDAO.searchSongsAdvanced("Estate", null, "Jazz");
        assertFalse(results.isEmpty(), "Dovrebbe trovare brani che corrispondono a testo e genere");
    }

    @Test
    @DisplayName("Ricerca case-insensitive su titolo e artista")
    public void testSearchAdvancedCaseInsensitive() {
        Song song = new Song(0, "Anima Fragile", "Vasco Rossi", "Rock", "", "", "", "", testUserId, "Luca", "Ricci", "");
        songDAO.addSong(song);

        List<Song> r1 = songDAO.searchSongs("anima fragile");
        List<Song> r2 = songDAO.searchSongs("ANIMA FRAGILE");
        List<Song> r3 = songDAO.searchSongs("Anima Fragile");

        assertFalse(r1.isEmpty(), "Dovrebbe trovare con minuscole");
        assertFalse(r2.isEmpty(), "Dovrebbe trovare con maiuscole");
        assertFalse(r3.isEmpty(), "Dovrebbe trovare con case misto");
    }

    @Test
    @DisplayName("Recupero brano per ID")
    public void testGetSongById() {
        Song song = new Song(0, "Nessun dorma", "Luciano Pavarotti", "Musica classica", "", "", "", "", testUserId, "Luca", "Ricci", "");
        songDAO.addSong(song);

        List<Song> all = songDAO.getAllSongs();
        int id = all.get(all.size() - 1).getId();

        Song found = songDAO.getSongById(id);
        assertNotNull(found);
        assertEquals("Nessun dorma", found.getTitle());
        assertEquals(testUserId, found.getUploader_id(), "Il brano deve essere legato all'utente corretto");
    }

    @Test
    @DisplayName("Generi distinti restituiti dal database")
    public void testGetDistinctGenres() {
        List<String> genres = songDAO.getDistinctGenres();
        assertNotNull(genres);
        assertTrue(genres.size() > 0);
        assertTrue(genres.contains("Rock") || genres.contains("Pop") || genres.contains("Jazz"));
    }

    @Test
    @DisplayName("Artisti distinti restituiti dal database")
    public void testGetDistinctAuthors() {
        List<String> authors = songDAO.getDistinctAuthors();
        assertNotNull(authors);
        assertFalse(authors.isEmpty(), "Dovrebbero esserci artisti dal seed");
    }

    @Test
    @DisplayName("Titoli distinti restituiti dal database")
    public void testGetDistinctTitles() {
        List<String> titles = songDAO.getDistinctTitles();
        assertNotNull(titles);
        assertFalse(titles.isEmpty(), "Dovrebbero esserci titoli dal seed");
    }

    @Test
    @DisplayName("Brani commentati dall'utente")
    public void testGetSongsCommentedByUser() {
        List<Song> commentedSongs = songDAO.getSongsCommentedByUser(testUserId);
        assertNotNull(commentedSongs);
        assertFalse(commentedSongs.isEmpty(), "L'utente ha già commentato una canzone nel seed");
    }

    @Test
    @DisplayName("Salvataggio e recupero dei path allegati al brano")
    public void testSongWithFiles() {
        Song song = new Song(
                0, "Sinfonia n.9", "Ludwig van Beethoven", "Musica classica",
                "/spartiti/sinfonia9.pdf",
                "/audio/sinfonia9.mp3",
                "https://youtube.com/watch?v=_4IRMYuE1hI",
                "/copertine/beethoven.jpg",
                testUserId, "Luca", "Ricci", "La celebre nona sinfonia"
        );

        songDAO.addSong(song);

        List<Song> all = songDAO.getAllSongs();
        Song retrieved = all.get(all.size() - 1);

        assertEquals("/spartiti/sinfonia9.pdf", retrieved.getPdfSheetPath());
        assertEquals("/audio/sinfonia9.mp3", retrieved.getAudioPath());
        assertEquals("https://youtube.com/watch?v=_4IRMYuE1hI", retrieved.getYoutubeUrl());
        assertEquals("/copertine/beethoven.jpg", retrieved.getCoverPath());
    }

    @Test
    @DisplayName("Descrizione del brano salvata correttamente")
    public void testSongDescription() {
        String descrizione = "Una delle ballate più intense di De André, testo straziante e melodia indimenticabile";
        Song song = new Song(
                0, "La guerra di Piero", "Fabrizio De André", "Folk",
                "", "", "", "",
                testUserId, "Luca", "Ricci", descrizione
        );

        songDAO.addSong(song);

        List<Song> all = songDAO.getAllSongs();
        Song retrieved = all.get(all.size() - 1);

        assertEquals(descrizione, retrieved.getDescription());
    }
}
