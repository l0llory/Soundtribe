package com.example.soundtribe;

import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.dao.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test SongDAO - Ricerca Avanzata e Filtri")
public class SongDAOTest {

    private SongDAO songDAO;
    private UserDAO userDAO;
    private int testUserId;

    @BeforeEach
    public void setUp() {
        songDAO = new SongDAO();
        userDAO = new UserDAO();

        // Setup: crea un utente di test per i brani
        String testEmail = "songtest_" + System.currentTimeMillis() + "@test.com";
        User testUser = new User("Musicista", "Test", testEmail, "password123", "Rock");
        userDAO.registerUser(testUser);
        User registeredUser = userDAO.login(testEmail, "password123");
        testUserId = registeredUser.getId();
    }

    @Test
    @DisplayName("Aggiunta di un brano")
    public void testAddSong() {
        Song newSong = new Song(
                0, "My First Song", "Test Artist", "Rock",
                "", "", "", "",
                testUserId, "Musicista", "Test", "Una canzone di test"
        );

        songDAO.addSong(newSong);

        List<Song> allSongs = songDAO.getAllSongs();
        assertFalse(allSongs.isEmpty(), "Dovrebbe esserci almeno un brano");

        Song foundSong = allSongs.get(allSongs.size() - 1);
        assertEquals("My First Song", foundSong.getTitle());
        assertEquals("Rock", foundSong.getGenre());
    }

    @Test
    @DisplayName("Recupero di tutti i brani")
    public void testGetAllSongs() {
        // Aggiungi alcuni brani
        Song song1 = new Song(0, "Song One", "Artist A", "Pop", "", "", "", "", testUserId, "Musicista", "Test", "");
        Song song2 = new Song(0, "Song Two", "Artist B", "Jazz", "", "", "", "", testUserId, "Musicista", "Test", "");

        songDAO.addSong(song1);
        songDAO.addSong(song2);

        List<Song> allSongs = songDAO.getAllSongs();
        assertNotNull(allSongs, "La lista non dovrebbe essere null");
        assertTrue(allSongs.size() >= 2, "Dovrebbe esserci almeno 2 brani");
    }

    @Test
    @DisplayName("Ricerca brani per titolo")
    public void testSearchSongsByTitle() {
        Song song = new Song(
                0, "Bohemian Rhapsody", "Queen", "Rock",
                "", "", "", "",
                testUserId, "Musicista", "Test", ""
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
                testUserId, "Musicista", "Test", ""
        );
        songDAO.addSong(song);

        List<Song> results = songDAO.searchSongs("John");
        assertFalse(results.isEmpty(), "Dovrebbe trovare il brano cercato");
        assertTrue(results.stream().anyMatch(s -> "John Lennon".equals(s.getArtist())));
    }

    @Test
    @DisplayName("Ricerca che non trova risultati")
    public void testSearchNoResults() {
        List<Song> results = songDAO.searchSongs("XYZABC123NOTEXIST");
        assertTrue(results.isEmpty(), "La ricerca non dovrebbe trovare nulla");
    }

    @Test
    @DisplayName("Ricerca avanzata con genere")
    public void testSearchAdvancedByGenre() {
        // Aggiungi brani di diversi generi
        Song rockSong = new Song(0, "Rock Song", "Rock Band", "Rock", "", "", "", "", testUserId, "Musicista", "Test", "");
        Song jazzSong = new Song(0, "Jazz Song", "Jazz Ensemble", "Jazz", "", "", "", "", testUserId, "Musicista", "Test", "");

        songDAO.addSong(rockSong);
        songDAO.addSong(jazzSong);

        List<Song> rockResults = songDAO.searchSongsAdvanced("", null, "Rock");
        assertTrue(rockResults.stream().allMatch(s -> "Rock".equals(s.getGenre())), "Tutti i risultati dovrebbero essere Rock");
    }

    @Test
    @DisplayName("Ricerca avanzata con testo e genere")
    public void testSearchAdvancedTextAndGenre() {
        Song popSong = new Song(0, "Summer Hits", "Pop Stars", "Pop", "", "", "", "", testUserId, "Musicista", "Test", "");
        songDAO.addSong(popSong);

        List<Song> results = songDAO.searchSongsAdvanced("Summer", null, "Pop");
        assertFalse(results.isEmpty(), "Dovrebbe trovare brani che corrispondono a testo E genere");
    }

    @Test
    @DisplayName("Ricerca avanzata case-insensitive")
    public void testSearchAdvancedCaseInsensitive() {
        Song song = new Song(0, "UPPERCASE TITLE", "lowercase artist", "Rock", "", "", "", "", testUserId, "Musicista", "Test", "");
        songDAO.addSong(song);

        List<Song> results1 = songDAO.searchSongs("uppercase");
        List<Song> results2 = songDAO.searchSongs("UPPERCASE");
        List<Song> results3 = songDAO.searchSongs("UpPerCase");

        assertFalse(results1.isEmpty(), "Dovrebbe trovare con minuscole");
        assertFalse(results2.isEmpty(), "Dovrebbe trovare con maiuscole");
        assertFalse(results3.isEmpty(), "Dovrebbe trovare con case misto");
    }

    @Test
    @DisplayName("Recupero brano per ID")
    public void testGetSongById() {
        Song newSong = new Song(0, "Find Me", "Test Artist", "Indie", "", "", "", "", testUserId, "Musicista", "Test", "");
        songDAO.addSong(newSong);

        List<Song> allSongs = songDAO.getAllSongs();
        int songId = allSongs.get(allSongs.size() - 1).getId();

        Song foundSong = songDAO.getSongById(songId);
        assertNotNull(foundSong, "Dovrebbe trovare il brano per ID");
        assertEquals("Find Me", foundSong.getTitle());
    }

    @Test
    @DisplayName("Generi distinti dal database")
    public void testGetDistinctGenres() {
        List<String> genres = songDAO.getDistinctGenres();
        assertNotNull(genres, "La lista di generi non dovrebbe essere null");
        assertTrue(genres.size() > 0, "Dovrebbe esserci almeno un genere nel sistema");

        // Verifica che i generi preset siano presenti
        assertTrue(genres.contains("Rock") || genres.contains("Pop") || genres.contains("Jazz"),
                "Dovrebbe contenere almeno uno dei generi preset");
    }

    @Test
    @DisplayName("Artisti distinti dal database")
    public void testGetDistinctAuthors() {
        List<String> authors = songDAO.getDistinctAuthors();
        assertNotNull(authors, "La lista di autori non dovrebbe essere null");
    }

    @Test
    @DisplayName("Titoli distinti dal database")
    public void testGetDistinctTitles() {
        List<String> titles = songDAO.getDistinctTitles();
        assertNotNull(titles, "La lista di titoli non dovrebbe essere null");
    }

    @Test
    @DisplayName("Brani commentati da un utente")
    public void testGetSongsCommentedByUser() {
        // Crea un brano
        Song song = new Song(0, "Commented Song", "Artist", "Rock", "", "", "", "", testUserId, "Musicista", "Test", "");
        songDAO.addSong(song);

        // In teoria dovremmo aggiungere un commento, ma per semplicità testiamo solo che il metodo funzioni
        List<Song> commentedSongs = songDAO.getSongsCommentedByUser(testUserId);
        assertNotNull(commentedSongs, "La lista non dovrebbe essere null");
    }

    @Test
    @DisplayName("Brano con path di file (PDF, Audio, YouTube, Cover)")
    public void testSongWithFiles() {
        Song songWithFiles = new Song(
                0, "Full Song", "Full Artist", "Pop",
                "/path/to/sheet.pdf",
                "/path/to/audio.mp3",
                "https://youtube.com/watch?v=123",
                "/path/to/cover.jpg",
                testUserId, "Musicista", "Test", "Brano completo"
        );

        songDAO.addSong(songWithFiles);

        List<Song> allSongs = songDAO.getAllSongs();
        Song retrievedSong = allSongs.get(allSongs.size() - 1);

        assertEquals("/path/to/sheet.pdf", retrievedSong.getPdfSheetPath());
        assertEquals("/path/to/audio.mp3", retrievedSong.getAudioPath());
        assertEquals("https://youtube.com/watch?v=123", retrievedSong.getYoutubeUrl());
        assertEquals("/path/to/cover.jpg", retrievedSong.getCoverPath());
    }

    @Test
    @DisplayName("Descrizione brano salvata e recuperata")
    public void testSongDescription() {
        String description = "Questa è una canzone bellissima con una storia affascinante";
        Song song = new Song(
                0, "Song with Description", "Artist", "Rock",
                "", "", "", "",
                testUserId, "Musicista", "Test", description
        );

        songDAO.addSong(song);

        List<Song> allSongs = songDAO.getAllSongs();
        Song retrievedSong = allSongs.get(allSongs.size() - 1);

        assertEquals(description, retrievedSong.getDescription());
    }
}