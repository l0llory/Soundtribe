package com.example.soundtribe;

import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.dao.*;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.manager.CommentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
        resetDatabase();
        commentDAO = new CommentDAO();
        songDAO    = new SongDAO();
        userDAO    = new UserDAO();

        // Popola il DB con 10 utenti fittizi, ognuno con una canzone e un commento
        int[] userIds = TestDataSeeder.seed(userDAO, songDAO, commentDAO);
        testUserId = userIds[0]; // Luca Ricci (ID=2)

        // Canzone fresca senza commenti preesistenti: usata dai test sulle asserzioni
        // precise su commenti (get(0), size, ecc.)
        Song freshSong = new Song(
                0, "Wish You Were Here", "Pink Floyd", "Rock", "", "", "", "",
                testUserId, "Luca", "Ricci", "Canzone di test senza commenti preesistenti"
        );
        songDAO.addSong(freshSong);
        List<Song> songs = songDAO.getAllSongs();
        testSongId = songs.get(songs.size() - 1).getId(); // ultima inserita, sicuramente senza commenti
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
    @DisplayName("Aggiunta di un commento principale")
    public void testAddRootComment() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Che arrangiamento bellissimo, non conoscevo questo artista", 0, null, "Pending"
        );

        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(comments.isEmpty());
        assertEquals("Che arrangiamento bellissimo, non conoscevo questo artista", comments.get(0).getContent());
    }

    @Test
    @DisplayName("Sistema ricorsivo di risposte (replies)")
    public void testRecursiveReplies() {
        Comment root = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "L'intro è fantastica, chi ha trascritto lo spartito?", 0, null, "Pending"
        );
        commentDAO.addComment(root, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> roots = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(roots.isEmpty());
        int parentId = roots.get(0).getId();

        Comment reply = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Ho caricato io lo spartito, grazie mille!", 0, parentId, "Pending"
        );
        commentDAO.addComment(reply, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> reloaded = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertFalse(reloaded.get(0).getReplies().isEmpty());
        assertEquals("Ho caricato io lo spartito, grazie mille!", reloaded.get(0).getReplies().get(0).getContent());
    }

    @Test
    @DisplayName("Profondità ricorsiva multipla (nested replies)")
    public void testMultipleNestedLevels() {
        Comment lvl0 = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Qualcuno sa il bpm esatto di questo pezzo?", 0, null, "Pending"
        );
        commentDAO.addComment(lvl0, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> lvl0List = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int lvl0Id = lvl0List.get(0).getId();

        Comment lvl1 = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Dovrebbe essere intorno ai 120, almeno nella versione originale", 0, lvl0Id, "Pending"
        );
        commentDAO.addComment(lvl1, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> reloaded = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int lvl1Id = reloaded.get(0).getReplies().get(0).getId();

        Comment lvl2 = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Sì esatto, io ho suonato a 118 e stava bene lo stesso", 0, lvl1Id, "Pending"
        );
        commentDAO.addComment(lvl2, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> final0 = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        Comment c0 = final0.get(0);
        assertEquals("Qualcuno sa il bpm esatto di questo pezzo?", c0.getContent());
        assertEquals(1, c0.getReplies().size());

        Comment c1 = c0.getReplies().get(0);
        assertEquals("Dovrebbe essere intorno ai 120, almeno nella versione originale", c1.getContent());
        assertEquals(1, c1.getReplies().size());

        Comment c2 = c1.getReplies().get(0);
        assertEquals("Sì esatto, io ho suonato a 118 e stava bene lo stesso", c2.getContent());
    }

    @Test
    @DisplayName("Toggle Like su un commento")
    public void testToggleLike() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Finalmente qualcuno che ha caricato questo pezzo!", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();
        int likesBefore = comments.get(0).getLikes();

        commentDAO.toggleLike(testUserId, commentId);

        List<Comment> reloaded = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertEquals(likesBefore + 1, reloaded.get(0).getLikes());
    }

    @Test
    @DisplayName("Controllo se utente ha già messo like")
    public void testHasUserLiked() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Bellissima versione acustica, meglio dell'originale", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();

        assertFalse(commentDAO.hasUserLiked(testUserId, commentId));

        commentDAO.toggleLike(testUserId, commentId);

        assertTrue(commentDAO.hasUserLiked(testUserId, commentId));
    }

    @Test
    @DisplayName("Eliminazione di un commento")
    public void testDeleteComment() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "ops ho scritto nel brano sbagliato", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> before = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = before.get(0).getId();
        int sizeBefore = before.size();

        commentDAO.deleteComment(commentId);

        List<Comment> after = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        assertEquals(sizeBefore - 1, after.size());
    }

    @Test
    @DisplayName("Solo l'autore può eliminare il proprio commento")
    public void testCanDeleteComment() {
        Comment c = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Questo brano lo suono da anni, grazie per lo spartito", 0, null, "Pending"
        );
        commentDAO.addComment(c, CommentManager.ResourceType.SONG, testSongId);

        List<Comment> comments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);
        int commentId = comments.get(0).getId();

        assertTrue(commentDAO.canDeleteComment(commentId, testUserId), "L'autore dovrebbe poter eliminare il proprio commento");
        assertFalse(commentDAO.canDeleteComment(commentId, 99999), "Un altro utente non dovrebbe poter eliminare il commento");
    }

    @Test
    @DisplayName("Commenti su risorse diverse (SONG, EXECUTION, CONCERT) non si mescolano")
    public void testCommentsOnDifferentResources() {
        Comment songComment = new Comment(
                0, testSongId, 0, 0,
                testUserId,
                "Commento sul brano", 0, null, "Pending"
        );
        commentDAO.addComment(songComment, CommentManager.ResourceType.SONG, testSongId);

        Comment execComment = new Comment(
                0, 0, 1, 0,
                testUserId,
                "Commento sull'esecuzione", 0, null, "Pending"
        );
        commentDAO.addComment(execComment, CommentManager.ResourceType.EXECUTION, 1);

        List<Comment> songComments = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, testSongId);

        assertFalse(songComments.isEmpty(), "Dovrebbe esserci il commento sul brano");
        assertTrue(songComments.stream().noneMatch(c -> "Commento sull'esecuzione".equals(c.getContent())),
                "I commenti di risorse diverse non devono mescolarsi");
    }
}
