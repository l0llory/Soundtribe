package com.example.soundtribe.system;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.ConcertDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.ExecutionDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.Concert;
import com.example.soundtribe.entita.ConcertTrack;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.manager.CommentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@DisplayName("System Test - Ciclo di vita di un concerto (Upload → Scaletta → Commenti)")
public class ConcertLifecycleSystemTest {

    private UserDAO userDAO;
    private ConcertDAO concertDAO;
    private CommentDAO commentDAO;
    private int uploaderId;
    private int viewerId;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        // ExecutionDAO deve essere inizializzato prima di ConcertDAO per creare la tabella instruments
        new ExecutionDAO();
        userDAO    = new UserDAO();
        concertDAO = new ConcertDAO();
        commentDAO = new CommentDAO();

        userDAO.registerUser(new User("Sofia", "Organizer", "sofia.org@gmail.com", "pw1", "Jazz"));
        userDAO.registerUser(new User("Nico", "Fan", "nico.fan@gmail.com", "pw2", "Blues"));

        uploaderId = userDAO.login("sofia.org@gmail.com", "pw1").getId();
        viewerId   = userDAO.login("nico.fan@gmail.com", "pw2").getId();
    }

    private static void resetDatabase() {
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {
            // CASCADE da concerts propaga a concert_tracks, che propaga a track_instruments
            stmt.execute("TRUNCATE comment_likes, comments, concerts, songs, users RESTART IDENTITY CASCADE");
            stmt.execute("INSERT INTO users (name, surname, email, password, is_admin, status) " +
                         "VALUES ('Admin', 'SoundTribe', 'admin@soundtribe.it', 'admin', TRUE, 'Verified')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Flusso completo: upload concerto con scaletta → recupero tracce in ordine")
    public void testConcertUploadAndTracksFlow() {
        Concert concert = new Concert(
                0, "Kind of Blue Live", "Miles Davis Sextet",
                "https://youtube.com/watch?v=kind_of_blue",
                Date.valueOf("1959-08-17"),
                "Carnegie Hall, New York",
                "La registrazione dal vivo del leggendario album", uploaderId
        );
        concert.addTrack(new ConcertTrack("So What", "Miles Davis", "0:00", "9:22", "Pianoforte, Contrabbasso"));
        concert.addTrack(new ConcertTrack("Blue in Green", "Miles Davis", "9:22", "16:10", "Pianoforte"));
        concertDAO.addConcert(concert);

        List<Concert> allConcerts = concertDAO.getAllConcerts();
        assertFalse(allConcerts.isEmpty());
        Concert saved = allConcerts.get(0);
        assertEquals("Kind of Blue Live", saved.getTitle());
        assertEquals("Miles Davis Sextet", saved.getArtist());
        assertEquals(uploaderId, saved.getUploaderId());

        List<ConcertTrack> savedTracks = concertDAO.getTracksForConcert(saved.getId());
        assertEquals(2, savedTracks.size());
        assertEquals("So What",       savedTracks.get(0).getTitle());
        assertEquals("Blue in Green", savedTracks.get(1).getTitle());
    }

    @Test
    @DisplayName("Flusso: aggiunta di tracce a un concerto esistente")
    public void testAddTracksToConcertAfterUploadFlow() {
        Concert concert = new Concert(
                0, "Woodstock 1969", "Various Artists",
                "https://youtube.com/watch?v=woodstock",
                Date.valueOf("1969-08-15"),
                "Bethel, New York", "Il festival più iconico della storia del rock", uploaderId
        );
        concert.setTracks(null);
        concertDAO.addConcert(concert);
        int concertId = concertDAO.getAllConcerts().get(0).getId();

        assertTrue(concertDAO.getTracksForConcert(concertId).isEmpty());

        concertDAO.addTrackToConcert(concertId,
                new ConcertTrack("Purple Haze", "Jimi Hendrix", "0:00", "4:11", "Chitarra elettrica, Basso, Batteria"));
        concertDAO.addTrackToConcert(concertId,
                new ConcertTrack("White Rabbit", "Jefferson Airplane", "4:11", "7:30", "Chitarra acustica"));

        List<ConcertTrack> tracks = concertDAO.getTracksForConcert(concertId);
        assertEquals(2, tracks.size());
        assertTrue(tracks.stream().anyMatch(t -> "Purple Haze".equals(t.getTitle())));
        assertTrue(tracks.stream().anyMatch(t -> "White Rabbit".equals(t.getTitle())));

        ConcertTrack purpleHaze = tracks.stream()
                .filter(t -> "Purple Haze".equals(t.getTitle())).findFirst().orElse(null);
        assertNotNull(purpleHaze);
        assertFalse(purpleHaze.getInstruments().isEmpty(), "Gli strumenti della traccia devono essere salvati");
    }

    @Test
    @DisplayName("Flusso: commento su un concerto → recuperabile e isolato da altri tipi di risorsa")
    public void testConcertCommentIsolationFlow() {
        Concert concert = new Concert(
                0, "Live at Pompeii", "Pink Floyd",
                "https://youtube.com/watch?v=pompeii",
                Date.valueOf("1972-10-04"),
                "Anfiteatro di Pompei", "Il leggendario concerto senza pubblico", uploaderId
        );
        concert.setTracks(null);
        concertDAO.addConcert(concert);
        int concertId = concertDAO.getAllConcerts().get(0).getId();

        commentDAO.addComment(
                new Comment(0, 0, 0, concertId, viewerId,
                        "Un'esperienza visiva e sonora unica, mai eguagliata", 0, null, "Pending"),
                CommentManager.ResourceType.CONCERT, concertId
        );

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.CONCERT, concertId);
        assertEquals(1, comments.size());
        assertEquals("Un'esperienza visiva e sonora unica, mai eguagliata", comments.get(0).getContent());
        assertEquals(viewerId, comments.get(0).getUserId());

        assertTrue(commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, concertId).isEmpty(),
                "Il commento al concerto non deve apparire nella lista song");
    }

    @Test
    @DisplayName("Flusso: più concerti dello stesso organizzatore → tutti recuperabili")
    public void testMultipleConcertsByOrganizerFlow() {
        for (int year = 2023; year <= 2025; year++) {
            Concert c = new Concert(0, "Jazz Night " + year, "Various",
                    "https://youtube.com/watch?v=jn" + year,
                    Date.valueOf(year + "-06-15"), "Blue Note, Milano",
                    year + " edizione", uploaderId);
            c.setTracks(null);
            concertDAO.addConcert(c);
        }

        List<Concert> all = concertDAO.getAllConcerts();
        assertEquals(3, all.size());
        assertTrue(all.stream().allMatch(c -> c.getUploaderId() == uploaderId));
    }

    @Test
    @DisplayName("Flusso: scaletta con strumenti non standard → strumenti salvati nel dizionario globale")
    public void testConcertTrackInstrumentsSavedToDictionaryFlow() {
        Concert concert = new Concert(0, "Fusion Night", "Ensemble",
                "https://youtube.com/watch?v=fn", Date.valueOf("2024-11-10"),
                "Teatro Nuovo, Roma", "Serata di musica fusion", uploaderId);
        concert.addTrack(new ConcertTrack("Nuova composizione", "Ensemble",
                "0:00", "8:00", "Theremin, Vibrafono, Contrabbasso"));
        concertDAO.addConcert(concert);

        List<String> instruments = new ExecutionDAO().getDistinctInstruments();
        assertTrue(instruments.contains("Theremin"),    "Il Theremin deve essere aggiunto al dizionario");
        assertTrue(instruments.contains("Vibrafono"),   "Il Vibrafono deve essere aggiunto al dizionario");
        assertTrue(instruments.contains("Contrabbasso"),"Il Contrabbasso deve essere nel dizionario");
    }
}
