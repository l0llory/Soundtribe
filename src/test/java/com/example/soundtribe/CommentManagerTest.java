package com.example.soundtribe;

import com.example.soundtribe.dao.*;
import com.example.soundtribe.manager.*;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.item.UserSession;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test CommentManager - Manager Pattern")
public class CommentManagerTest {

    private CommentManager commentManager;
    private CommentDAO commentDAO;
    private SongDAO songDAO;
    private UserDAO userDAO;
    private int testSongId;
    private int testUserId;

    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    public void setUp() {
        resetDatabase();
        commentDAO = new CommentDAO();
        songDAO    = new SongDAO();
        userDAO    = new UserDAO();

        // Popola il DB con 10 utenti fittizi, ognuno con una canzone e un commento
        int[] userIds = TestDataSeeder.seed(userDAO, songDAO, commentDAO);
        testUserId = userIds[1]; // Sara Conti (ID=3) — seconda utente del seeder

        UserSession.getInstance().setUserId(testUserId);
        UserSession.getInstance().setIsAdmin(false);

        // Canzone fresca senza commenti preesistenti per test precisi
        Song freshSong = new Song(
                0, "Kind of Blue", "Miles Davis", "Jazz", "", "", "", "",
                testUserId, "Sara", "Conti", "Uno degli album jazz più influenti di sempre"
        );
        songDAO.addSong(freshSong);
        List<Song> songs = songDAO.getAllSongs();
        testSongId = songs.get(songs.size() - 1).getId();

        commentManager = new CommentManager(
                new VBox(), new TextArea(), new Button("Commenta"),
                testSongId, CommentManager.ResourceType.SONG
        );
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
    @DisplayName("Inizializzazione del CommentManager")
    public void testCommentManagerInitialization() {
        assertNotNull(commentManager);
    }

    @Test
    @DisplayName("Commenti caricati dal database all'inizializzazione")
    public void testLoadCommentsOnInit() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Bellissimo disco, l'ho ascoltato mille volte", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        new CommentManager(new VBox(), new TextArea(), new Button(), testSongId, CommentManager.ResourceType.SONG);

        List<Comment> loaded = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(loaded.isEmpty());
    }

    @Test
    @DisplayName("Manager creabile per tutte e tre le risorse")
    public void testCommentManagerResourceTypes() {
        CommentManager songManager = new CommentManager(
                new VBox(), new TextArea(), new Button(), testSongId, CommentManager.ResourceType.SONG
        );
        assertNotNull(songManager);

        CommentManager execManager = new CommentManager(
                new VBox(), new TextArea(), new Button(), 1, CommentManager.ResourceType.EXECUTION
        );
        assertNotNull(execManager);

        CommentManager concertManager = new CommentManager(
                new VBox(), new TextArea(), new Button(), 1, CommentManager.ResourceType.CONCERT
        );
        assertNotNull(concertManager);
    }

    @Test
    @DisplayName("Sessione utente correttamente usata dal Manager")
    public void testCommentManagerUserSession() {
        UserSession.getInstance().setUserId(testUserId);

        CommentManager manager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                testSongId, CommentManager.ResourceType.SONG
        );

        assertNotNull(manager);
        assertEquals(testUserId, UserSession.getInstance().getUserId());
    }

    @Test
    @DisplayName("Caricamento struttura ricorsiva di commenti e risposte")
    public void testDynamicCommentLoading() {
        Comment root = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Qualcuno sa dove trovare lo spartito del tema principale?", 0, null, "Pending"
        );
        commentDAO.addComment(root, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> roots = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int rootId = roots.get(0).getId();

        Comment reply = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "L'ho trovato su IMSLP, ti metto il link", 0, rootId, "Pending"
        );
        commentDAO.addComment(reply, CommentManager.ResourceType.SONG, testSongId);

        new CommentManager(new VBox(), new TextArea(), new Button(), testSongId, CommentManager.ResourceType.SONG);

        List<Comment> reloaded = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(reloaded.isEmpty());
        assertFalse(reloaded.get(0).getReplies().isEmpty());
    }

    @Test
    @DisplayName("Integrazione CommentManager e CommentDAO - salvataggio e recupero")
    public void testCommentManagerDAOIntegration() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Primo ascolto di questo disco, già innamorata", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertTrue(comments.stream().anyMatch(x -> "Primo ascolto di questo disco, già innamorata".equals(x.getContent())));
    }

    @Test
    @DisplayName("Aggiornamento dello status del commento da Pending a Verified")
    public void testCommentStatusHandling() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Assolo di tromba magistrale nella terza traccia", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();

        commentDAO.updateCommentStatus(commentId, "Verified");

        List<Comment> verified = commentDAO.getCommentsListByStatus("Verified");
        assertTrue(verified.stream().anyMatch(x -> x.getId() == commentId));
    }
}
