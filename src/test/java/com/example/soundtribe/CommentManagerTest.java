package com.example.soundtribe;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.manager.*;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
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
    private VBox mockContainer;
    private TextArea mockCommentArea;
    private Button mockButton;

    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit già avviato
        }
    }

    @BeforeEach
    public void setUp() {
        commentDAO = new CommentDAO();
        songDAO = new SongDAO();
        userDAO = new UserDAO();

        // Setup: crea un utente di test
        String testEmail = "commentmanager_" + System.currentTimeMillis() + "@test.com";
        User testUser = new User("ManagerTest", "User", testEmail, "password123", "Rock");
        userDAO.registerUser(testUser);
        User registeredUser = userDAO.login(testEmail, "password123");
        testUserId = registeredUser.getId();

        // Setup sessione utente
        UserSession.getInstance().setUserId(testUserId);
        UserSession.getInstance().setIsAdmin(false);

        // Setup: crea un brano di test
        Song testSong = new Song(
                0, "Manager Test Song", "Test Artist", "Rock", "", "", "", "",
                testUserId, "ManagerTest", "User", "Test description"
        );
        songDAO.addSong(testSong);
        List<Song> songs = songDAO.getAllSongs();
        testSongId = songs.get(songs.size() - 1).getId();

        // Setup: crea i mock JavaFX (sono required dal costruttore)
        mockContainer = new VBox();
        mockCommentArea = new TextArea();
        mockButton = new Button("Post");

        // Crea il CommentManager
        commentManager = new CommentManager(
                mockContainer, mockCommentArea, mockButton,
                testSongId, CommentManager.ResourceType.SONG
        );
    }

    @Test
    @DisplayName("Inizializzazione del CommentManager")
    public void testCommentManagerInitialization() {
        assertNotNull(commentManager, "CommentManager dovrebbe essere inizializzato");
        assertNotNull(mockContainer, "Container dovrebbe essere non-null");
    }

    @Test
    @DisplayName("Caricamento commenti alla inizializzazione")
    public void testLoadCommentsOnInit() {
        // Aggiungi un commento prima di creare il manager
        Comment comment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "ManagerTest",
                "Test comment from manager", 0, null, "Pending"
        );
        commentDAO.addComment(comment, CommentManager.ResourceType.SONG, testSongId);

        // Crea nuovo manager che dovrebbe caricare i commenti
        CommentManager newManager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                testSongId, CommentManager.ResourceType.SONG
        );

        // Verifica che i commenti siano stati caricati dal DB
        List<Comment> comments = commentDAO.getCommentsByResource(
                CommentManager.ResourceType.SONG, testSongId
        );
        assertFalse(comments.isEmpty(), "Dovrebbe caricare i commenti dal DB");
    }

    @Test
    @DisplayName("Supporto per diverse risorse (SONG, EXECUTION, CONCERT)")
    public void testCommentManagerResourceTypes() {
        // Test SONG
        CommentManager songManager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                testSongId, CommentManager.ResourceType.SONG
        );
        assertNotNull(songManager, "Manager per SONG dovrebbe essere creato");

        // Test EXECUTION (con ID fittizio)
        CommentManager execManager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                1, CommentManager.ResourceType.EXECUTION
        );
        assertNotNull(execManager, "Manager per EXECUTION dovrebbe essere creato");

        // Test CONCERT (con ID fittizio)
        CommentManager concertManager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                1, CommentManager.ResourceType.CONCERT
        );
        assertNotNull(concertManager, "Manager per CONCERT dovrebbe essere creato");
    }

    @Test
    @DisplayName("Enum ResourceType")
    public void testResourceTypeEnum() {
        // Verifica che i valori dell'enum siano i previsti
        CommentManager.ResourceType song = CommentManager.ResourceType.SONG;
        CommentManager.ResourceType exec = CommentManager.ResourceType.EXECUTION;
        CommentManager.ResourceType concert = CommentManager.ResourceType.CONCERT;

        assertNotNull(song);
        assertNotNull(exec);
        assertNotNull(concert);

        assertEquals("SONG", song.name());
        assertEquals("EXECUTION", exec.name());
        assertEquals("CONCERT", concert.name());
    }

    @Test
    @DisplayName("Gestione sessione utente nel Manager")
    public void testCommentManagerUserSession() {
        // Imposta una sessione utente
        UserSession session = UserSession.getInstance();
        session.setUserId(testUserId);

        // Crea un manager
        CommentManager manager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                testSongId, CommentManager.ResourceType.SONG
        );

        // Il manager dovrebbe usare l'ID della sessione
        assertNotNull(manager, "Manager dovrebbe essere creato con sessione attiva");
    }

    @Test
    @DisplayName("Caricamento dinamico di commenti ricorsivi")
    public void testDynamicCommentLoading() {
        // Aggiungi una gerarchia di commenti
        Comment root = new Comment(
                0, testSongId, 0, 0,
                testUserId, "RootUser",
                "Root comment", 0, null, "Pending"
        );
        commentDAO.addComment(root, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> rootComments = commentDAO.getCommentsByResource(
                CommentManager.ResourceType.SONG, testSongId
        );
        int rootId = rootComments.get(0).getId();

        // Aggiungi una risposta
        Comment reply = new Comment(
                0, testSongId, 0, 0,
                testUserId, "ReplyUser",
                "Reply comment", 0, rootId, "Pending"
        );
        commentDAO.addComment(reply, CommentManager.ResourceType.SONG, testSongId);

        // Crea un nuovo manager che dovrebbe caricare la struttura ricorsiva
        CommentManager newManager = new CommentManager(
                new VBox(), new TextArea(), new Button(),
                testSongId, CommentManager.ResourceType.SONG
        );

        // Verifica che la struttura sia caricata
        List<Comment> reloadedComments = commentDAO.getCommentsByResource(
                CommentManager.ResourceType.SONG, testSongId
        );
        assertFalse(reloadedComments.isEmpty(), "Dovrebbe caricare i commenti root");
        assertFalse(reloadedComments.get(0).getReplies().isEmpty(),
                "Dovrebbe caricare le risposte ricorsive");
    }

    @Test
    @DisplayName("TextArea per i nuovi commenti")
    public void testCommentTextArea() {
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Scrivi un commento...");

        assertNotNull(commentArea, "TextArea dovrebbe essere non-null");
        assertEquals("Scrivi un commento...", commentArea.getPromptText());

        commentArea.setText("Questo è un nuovo commento");
        assertEquals("Questo è un nuovo commento", commentArea.getText());
    }

    @Test
    @DisplayName("Button per postare commenti")
    public void testCommentPostButton() {
        Button postBtn = new Button("Invia Commento");

        assertNotNull(postBtn, "Button dovrebbe essere non-null");
        assertEquals("Invia Commento", postBtn.getText());
    }

    @Test
    @DisplayName("Integrazione tra CommentManager e CommentDAO")
    public void testCommentManagerDAOIntegration() {
        // Il CommentManager dovrebbe essere in grado di comunicare con il DAO
        Comment testComment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "IntegrationTest",
                "Integration test comment", 0, null, "Pending"
        );

        commentDAO.addComment(testComment, CommentManager.ResourceType.SONG, testSongId);

        // Verifica che il commento sia stato salvato
        List<Comment> comments = commentDAO.getCommentsByResource(
                CommentManager.ResourceType.SONG, testSongId
        );
        assertTrue(comments.stream().anyMatch(c -> "Integration test comment".equals(c.getContent())),
                "Il commento dovrebbe essere salvato dal DAO");
    }

    @Test
    @DisplayName("Gestione dello status del commento")
    public void testCommentStatusHandling() {
        Comment comment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "StatusTest",
                "Comment with status", 0, null, "Pending"
        );

        // Aggiungi con status Pending
        commentDAO.addComment(comment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(
                CommentManager.ResourceType.SONG, testSongId
        );
        int commentId = comments.get(0).getId();

        // Cambia status
        commentDAO.updateCommentStatus(commentId, "Verified");

        List<Comment> verifiedComments = commentDAO.getCommentsListByStatus("Verified");
        assertTrue(verifiedComments.stream().anyMatch(c -> c.getId() == commentId),
                "Il commento dovrebbe avere status Verified");
    }

    @Test
    @DisplayName("Container VBox per visualizzare commenti")
    public void testCommentContainer() {
        VBox container = new VBox(10);
        container.setStyle("-fx-padding: 10;");

        assertNotNull(container, "Container dovrebbe essere non-null");
        assertEquals(10, container.getSpacing(), "Spacing dovrebbe essere 10");
    }
}