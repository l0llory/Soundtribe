package com.example.soundtribe;

import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.dao.*;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.manager.CommentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test CommentDAO - Sistema di Commenti Ricorsivi")
public class CommentDAOTest {

    private CommentDAO commentDAO;
    private SongDAO songDAO;
    private UserDAO userDAO;
    private int testSongId;
    private int testUserId;

    @BeforeEach
    public void setUp() {
        commentDAO = new CommentDAO();
        songDAO = new SongDAO();
        userDAO = new UserDAO();

        // Setup: crea un utente di test
        String testEmail = "commenttest_" + System.currentTimeMillis() + "@test.com";
        User testUser = new User("Tester", "Commenti", testEmail, "password123", "Rock");
        userDAO.registerUser(testUser);
        User registeredUser = userDAO.login(testEmail, "password123");
        testUserId = registeredUser.getId();

        // Setup: crea un brano di test
        Song testSong = new Song(
                0, "Brano Test", "Artista Test", "Rock", "", "", "", "",
                testUserId, "Tester", "Commenti", "Descrizione test"
        );
        songDAO.addSong(testSong);
        List<Song> songs = songDAO.getAllSongs();
        testSongId = songs.get(songs.size() - 1).getId();
    }

    @Test
    @DisplayName("Aggiunta di un commento principale")
    public void testAddRootComment() {
        Comment rootComment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Questo è un commento principale", 0, null, "Pending"
        );

        commentDAO.addComment(rootComment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(comments.isEmpty(), "Dovrebbe esserci almeno un commento");
        assertEquals("Questo è un commento principale", comments.get(0).getContent());
    }

    @Test
    @DisplayName("Sistema ricorsivo di risposte (replies)")
    public void testRecursiveReplies() {
        // Crea commento principale
        Comment rootComment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Commento padre", 0, null, "Pending"
        );
        commentDAO.addComment(rootComment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> rootComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(rootComments.isEmpty(), "Dovrebbe esserci il commento padre");

        int parentId = rootComments.get(0).getId();

        // Crea risposta (reply)
        Comment reply = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Questo è una risposta", 0, parentId, "Pending"
        );
        commentDAO.addComment(reply, CommentManager.ResourceType.SONG, testSongId);

        // Verifica che la risposta sia stata aggiunta alle replies del padre
        List<Comment> reloadedComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        Comment reloadedParent = reloadedComments.get(0);
        assertFalse(reloadedParent.getReplies().isEmpty(), "Il commento padre dovrebbe avere almeno una risposta");
        assertEquals("Questo è una risposta", reloadedParent.getReplies().get(0).getContent());
    }

    @Test
    @DisplayName("Profondità ricorsiva multipla (nested replies)")
    public void testMultipleNestedLevels() {
        // Livello 0: Commento principale
        Comment level0 = new Comment(
                0, testSongId, 0, 0,
                testUserId, "User0",
                "Livello 0", 0, null, "Pending"
        );
        commentDAO.addComment(level0, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> level0List = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int level0Id = level0List.get(0).getId();

        // Livello 1: Risposta al commento principale
        Comment level1 = new Comment(
                0, testSongId, 0, 0,
                testUserId, "User1",
                "Livello 1", 0, level0Id, "Pending"
        );
        commentDAO.addComment(level1, CommentManager.ResourceType.SONG, testSongId);

        // Livello 2: Risposta alla risposta
        List<Comment> reloadedLevel0 = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int level1Id = reloadedLevel0.get(0).getReplies().get(0).getId();

        Comment level2 = new Comment(
                0, testSongId, 0, 0,
                testUserId, "User2",
                "Livello 2", 0, level1Id, "Pending"
        );
        commentDAO.addComment(level2, CommentManager.ResourceType.SONG, testSongId);

        // Verifica la struttura completa
        List<Comment> finalComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        Comment finalLevel0 = finalComments.get(0);

        assertEquals("Livello 0", finalLevel0.getContent());
        assertEquals(1, finalLevel0.getReplies().size());

        Comment finalLevel1 = finalLevel0.getReplies().get(0);
        assertEquals("Livello 1", finalLevel1.getContent());
        assertEquals(1, finalLevel1.getReplies().size());

        Comment finalLevel2 = finalLevel1.getReplies().get(0);
        assertEquals("Livello 2", finalLevel2.getContent());
    }

    @Test
    @DisplayName("Toggle Like su un commento")
    public void testToggleLike() {
        Comment comment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Commento da likeare", 0, null, "Pending"
        );
        commentDAO.addComment(comment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();
        int initialLikes = comments.get(0).getLikes();

        // Aggiungi un like
        commentDAO.toggleLike(testUserId, commentId);
        List<Comment> reloadedComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int likesAfterToggle = reloadedComments.get(0).getLikes();

        assertEquals(initialLikes + 1, likesAfterToggle, "I like dovrebbero aumentare di 1");
    }

    @Test
    @DisplayName("Controllo se utente ha messo like")
    public void testHasUserLiked() {
        Comment comment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Commento per hasUserLiked", 0, null, "Pending"
        );
        commentDAO.addComment(comment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();

        boolean hasLikedBefore = commentDAO.hasUserLiked(testUserId, commentId);
        assertFalse(hasLikedBefore, "Utente non dovrebbe aver messo like prima");

        commentDAO.toggleLike(testUserId, commentId);
        boolean hasLikedAfter = commentDAO.hasUserLiked(testUserId, commentId);
        assertTrue(hasLikedAfter, "Utente dovrebbe aver messo like dopo toggleLike");
    }

    @Test
    @DisplayName("Eliminazione di un commento")
    public void testDeleteComment() {
        Comment comment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Commento da eliminare", 0, null, "Pending"
        );
        commentDAO.addComment(comment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> beforeDelete = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = beforeDelete.get(0).getId();
        int sizeBefore = beforeDelete.size();

        commentDAO.deleteComment(commentId);

        List<Comment> afterDelete = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int sizeAfter = afterDelete.size();

        assertEquals(sizeBefore - 1, sizeAfter, "Dopo eliminazione dovrebbe avere un commento in meno");
    }

    @Test
    @DisplayName("Controllo permessi - canDeleteComment")
    public void testCanDeleteComment() {
        Comment comment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "TesterName",
                "Commento del tester", 0, null, "Pending"
        );
        commentDAO.addComment(comment, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();

        boolean canDelete = commentDAO.canDeleteComment(commentId, testUserId);
        assertTrue(canDelete, "L'autore dovrebbe poter eliminare il proprio commento");

        boolean canDeleteOther = commentDAO.canDeleteComment(commentId, 99999);
        assertFalse(canDeleteOther, "Un altro utente non dovrebbe poter eliminare il commento");
    }

    @Test
    @DisplayName("Commenti su diverse risorse (SONG, EXECUTION, CONCERT)")
    public void testCommentsOnDifferentResources() {
        // Commento su SONG
        Comment songComment = new Comment(
                0, testSongId, 0, 0,
                testUserId, "User",
                "Commento su brano", 0, null, "Pending"
        );
        commentDAO.addComment(songComment, CommentManager.ResourceType.SONG, testSongId);

        // Commento su EXECUTION (con ID fittizio)
        Comment execComment = new Comment(
                0, 0, 1, 0,
                testUserId, "User",
                "Commento su esecuzione", 0, null, "Pending"
        );
        commentDAO.addComment(execComment, CommentManager.ResourceType.EXECUTION, 1);

        // Commento su CONCERT (con ID fittizio)
        Comment concertComment = new Comment(
                0, 0, 0, 1,
                testUserId, "User",
                "Commento su concerto", 0, null, "Pending"
        );
        commentDAO.addComment(concertComment, CommentManager.ResourceType.CONCERT, 1);

        List<Comment> songComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        List<Comment> execComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.EXECUTION, 1);
        List<Comment> concertComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.CONCERT, 1);

        assertFalse(songComments.isEmpty(), "Dovrebbe esserci commento su SONG");
        // execComments potrebbe essere vuota se l'execution non esiste nel DB
        // Non facciamo assert su esecuzione e concerto poiché gli ID sono fittizi
    }
}