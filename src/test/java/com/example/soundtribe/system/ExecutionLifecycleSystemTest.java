package com.example.soundtribe.system;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.ExecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.Execution;
import com.example.soundtribe.entita.ExecutionSegment;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.manager.CommentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@DisplayName("System Test - Ciclo di vita di un'esecuzione (Upload → Segmenti → Commenti)")
public class ExecutionLifecycleSystemTest {

    private UserDAO userDAO;
    private SongDAO songDAO;
    private ExecutionDAO executionDAO;
    private CommentDAO commentDAO;
    private int uploaderId;
    private int viewerId;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        userDAO      = new UserDAO();
        songDAO      = new SongDAO();
        executionDAO = new ExecutionDAO();
        commentDAO   = new CommentDAO();

        userDAO.registerUser(new User("Giulia", "Arpa", "giulia.arpa@gmail.com", "pw1", "Musica classica"));
        userDAO.registerUser(new User("Piero", "Viewer", "piero.viewer@gmail.com", "pw2", "Jazz"));

        uploaderId = userDAO.login("giulia.arpa@gmail.com", "pw1").getId();
        viewerId   = userDAO.login("piero.viewer@gmail.com", "pw2").getId();
    }

    private static void resetDatabase() {
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement()) {
            // CASCADE da media_files propaga a execution_instruments e execution_segments
            stmt.execute("TRUNCATE comment_likes, comments, media_files, songs, users RESTART IDENTITY CASCADE");
            stmt.execute("INSERT INTO users (name, surname, email, password, is_admin, status) " +
                         "VALUES ('Admin', 'SoundTribe', 'admin@soundtribe.it', 'admin', TRUE, 'Verified')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Flusso completo: upload esecuzione collegata a una canzone → recupero → segmento annotato")
    public void testExecutionUploadAndSegmentFlow() {
        songDAO.addSong(new Song(0, "Moonlight Sonata", "Beethoven", "Musica classica",
                "", "", "", "", uploaderId, "Giulia", "Arpa", ""));
        int songId = songDAO.getAllSongs().get(songDAO.getAllSongs().size() - 1).getId();

        executionDAO.addMedia(new Execution(
                0, songId, "Moonlight Sonata - Esecuzione di Giulia",
                "/video/moonlight_giulia.mp4", "video/mp4",
                "Giulia Arpa", "Pianoforte", "4:30", false, null,
                "Sala Prove Milano", false, true, uploaderId
        ));

        List<Execution> all = executionDAO.getAllExecutions();
        assertFalse(all.isEmpty());
        Execution retrieved = all.get(0);
        assertEquals("Moonlight Sonata - Esecuzione di Giulia", retrieved.getTitle());
        assertEquals(songId, retrieved.getSongId());
        assertEquals(uploaderId, retrieved.getUploaderId());

        ExecutionSegment segment = new ExecutionSegment();
        segment.setExecutionId(retrieved.getId());
        segment.setUserId(viewerId);
        segment.setStartTime("1:30");
        segment.setEndTime("2:00");
        segment.setNotes("Passaggio tecnico molto difficile - dita veloci nel secondo tema");
        executionDAO.addSegmentToExecution(segment);

        List<ExecutionSegment> segments = executionDAO.getSegmentsForExecution(retrieved.getId());
        assertEquals(1, segments.size());
        assertEquals("Passaggio tecnico molto difficile - dita veloci nel secondo tema",
                segments.get(0).getNotes());
        assertEquals(viewerId, segments.get(0).getUserId());
        assertEquals("1:30", segments.get(0).getStartTime());
        assertEquals("2:00",  segments.get(0).getEndTime());
    }

    @Test
    @DisplayName("Flusso: più segmenti sulla stessa esecuzione → ordinati per start_time")
    public void testMultipleSegmentsOnSameExecutionFlow() {
        executionDAO.addMedia(new Execution(0, 0, "Concerto n.1 in la minore", "/v/c1.mp4",
                "video/mp4", "Giulia Arpa", "Pianoforte", "25:00",
                false, null, "Conservatorio", false, true, uploaderId));
        int execId = executionDAO.getAllExecutions().get(0).getId();

        ExecutionSegment seg1 = new ExecutionSegment();
        seg1.setExecutionId(execId); seg1.setUserId(uploaderId);
        seg1.setStartTime("05:00"); seg1.setEndTime("07:30");
        seg1.setNotes("Cadenza principale - difficoltà elevata");

        ExecutionSegment seg2 = new ExecutionSegment();
        seg2.setExecutionId(execId); seg2.setUserId(viewerId);
        seg2.setStartTime("12:00"); seg2.setEndTime("15:00");
        seg2.setNotes("Tema lirico - bellissima interpretazione");

        ExecutionSegment seg3 = new ExecutionSegment();
        seg3.setExecutionId(execId); seg3.setUserId(viewerId);
        seg3.setStartTime("20:00"); seg3.setEndTime("22:30");
        seg3.setNotes("Coda conclusiva - accelerazione finale");

        executionDAO.addSegmentToExecution(seg1);
        executionDAO.addSegmentToExecution(seg2);
        executionDAO.addSegmentToExecution(seg3);

        List<ExecutionSegment> segments = executionDAO.getSegmentsForExecution(execId);
        assertEquals(3, segments.size());
        assertEquals("05:00", segments.get(0).getStartTime());
        assertEquals("12:00", segments.get(1).getStartTime());
        assertEquals("20:00", segments.get(2).getStartTime());
    }

    @Test
    @DisplayName("Flusso: ricerca esecuzioni per titolo e per esecutore")
    public void testExecutionSearchFlow() {
        executionDAO.addMedia(new Execution(0, 0, "Sonata al chiaro di luna", "/v/e1.mp4",
                "video/mp4", "Mario Rossi", "Pianoforte", "5:00",
                false, null, "Roma", false, true, uploaderId));
        executionDAO.addMedia(new Execution(0, 0, "Capriccio n.24", "/v/e2.mp4",
                "video/mp4", "Giulia Arpa", "Violino", "7:00",
                false, null, "Milano", false, true, uploaderId));
        executionDAO.addMedia(new Execution(0, 0, "Take Five", "/v/e3.mp4",
                "video/mp4", "Jazz Quartet", "Sax", "5:30",
                false, null, "Torino", false, false, uploaderId));

        List<Execution> byTitle = executionDAO.searchExecutions("Capriccio");
        assertFalse(byTitle.isEmpty());
        assertEquals("Capriccio n.24", byTitle.get(0).getTitle());

        assertTrue(executionDAO.searchExecutions("Jazz Quartet").stream()
                .anyMatch(e -> "Take Five".equals(e.getTitle())));

        assertTrue(executionDAO.searchExecutions("xyznotexist99").isEmpty());
    }

    @Test
    @DisplayName("Flusso: recupero esecuzioni per ID canzone")
    public void testGetExecutionsBySongIdFlow() {
        songDAO.addSong(new Song(0, "Canzone A", "Artista A", "Pop",
                "", "", "", "", uploaderId, "Giulia", "Arpa", ""));
        songDAO.addSong(new Song(0, "Canzone B", "Artista B", "Rock",
                "", "", "", "", uploaderId, "Giulia", "Arpa", ""));

        List<Song> songs = songDAO.getAllSongs();
        int songIdA = songs.get(songs.size() - 2).getId();
        int songIdB = songs.get(songs.size() - 1).getId();

        executionDAO.addMedia(new Execution(0, songIdA, "Esecuzione A1", "/v/a1.mp4",
                "video/mp4", "Giulia", "Pianoforte", "3:00", false, null, "Milano", false, true, uploaderId));
        executionDAO.addMedia(new Execution(0, songIdA, "Esecuzione A2", "/v/a2.mp4",
                "video/mp4", "Piero", "Pianoforte", "3:30", false, null, "Roma", false, false, viewerId));
        executionDAO.addMedia(new Execution(0, songIdB, "Esecuzione B1", "/v/b1.mp4",
                "video/mp4", "Giulia", "Violino", "4:00", false, null, "Torino", false, true, uploaderId));

        assertEquals(2, executionDAO.getMediaBySongId(songIdA).size());
        assertEquals(1, executionDAO.getMediaBySongId(songIdB).size());
        assertEquals("Esecuzione B1", executionDAO.getMediaBySongId(songIdB).get(0).getTitle());
    }

    @Test
    @DisplayName("Flusso: commento su esecuzione isolato dai commenti su canzoni")
    public void testCommentOnExecutionIsolatedFromSongsFlow() {
        songDAO.addSong(new Song(0, "Brano Separato", "Artista", "Rock",
                "", "", "", "", uploaderId, "Giulia", "Arpa", ""));
        int songId = songDAO.getAllSongs().get(0).getId();

        executionDAO.addMedia(new Execution(0, 0, "Esecuzione Separata", "/v/sep.mp4",
                "video/mp4", "Giulia", null, "3:00", false, null, "Casa", false, true, uploaderId));
        int execId = executionDAO.getAllExecutions().get(0).getId();

        commentDAO.addComment(
                new Comment(0, songId, 0, 0, viewerId, "Commento sul brano", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId
        );
        commentDAO.addComment(
                new Comment(0, 0, execId, 0, viewerId, "Commento sull'esecuzione", 0, null, "Pending"),
                CommentManager.ResourceType.EXECUTION, execId
        );

        List<Comment> songComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId);
        assertEquals(1, songComments.size());
        assertEquals("Commento sul brano", songComments.get(0).getContent());

        List<Comment> execComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.EXECUTION, execId);
        assertEquals(1, execComments.size());
        assertEquals("Commento sull'esecuzione", execComments.get(0).getContent());
    }

    @Test
    @DisplayName("Dizionario strumenti popolato con i preset all'inizializzazione del DAO")
    public void testInstrumentsDictionaryInitializedAtStartup() {
        List<String> instruments = executionDAO.getDistinctInstruments();
        assertFalse(instruments.isEmpty(), "Il dizionario strumenti deve contenere i preset");
        assertTrue(instruments.contains("Pianoforte"));
        assertTrue(instruments.contains("Violino"));
        assertTrue(instruments.contains("Chitarra elettrica"));
        assertTrue(instruments.contains("Batteria"));
    }
}
