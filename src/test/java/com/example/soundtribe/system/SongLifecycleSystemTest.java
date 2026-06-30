package com.example.soundtribe.system;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@DisplayName("System Test - Ciclo di vita di un brano (Upload → Ricerca → Commento → Moderazione)")
public class SongLifecycleSystemTest {

    private UserDAO userDAO;
    private SongDAO songDAO;
    private CommentDAO commentDAO;
    private int uploaderId;
    private int commenterId;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        userDAO    = new UserDAO();
        songDAO    = new SongDAO();
        commentDAO = new CommentDAO();

        userDAO.registerUser(new User("Lucia", "Upload", "lucia.upload@gmail.com", "pass1", "Rock"));
        userDAO.registerUser(new User("Marco", "Comment", "marco.comment@gmail.com", "pass2", "Jazz"));

        uploaderId  = userDAO.login("lucia.upload@gmail.com", "pass1").getId();
        commenterId = userDAO.login("marco.comment@gmail.com", "pass2").getId();
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
    @DisplayName("Flusso completo: upload brano con allegati → ricerca per titolo e artista → dettagli corretti")
    public void testUploadAndSearchFlow() {
        Song song = new Song(
                0, "Summertime", "George Gershwin", "Jazz",
                "/spartiti/summertime.pdf", "/audio/summertime.mp3",
                "https://youtube.com/watch?v=xyz", "/cover/summertime.jpg",
                uploaderId, "Lucia", "Upload", "Un classico del jazz americano"
        );
        songDAO.addSong(song);

        List<Song> byTitle = songDAO.searchSongs("Summertime");
        assertFalse(byTitle.isEmpty(), "Il brano deve essere trovato per titolo");
        assertEquals("Summertime", byTitle.get(0).getTitle());
        assertEquals(uploaderId, byTitle.get(0).getUploader_id());

        assertFalse(songDAO.searchSongs("Gershwin").isEmpty(), "Il brano deve essere trovato per artista");

        assertTrue(songDAO.searchSongsAdvanced("", null, "Jazz").stream()
                .anyMatch(s -> "Summertime".equals(s.getTitle())),
                "La ricerca avanzata per genere Jazz deve includere Summertime");

        Song retrieved = songDAO.getAllSongs().stream()
                .filter(s -> "Summertime".equals(s.getTitle()))
                .findFirst().orElse(null);
        assertNotNull(retrieved);
        assertEquals("/spartiti/summertime.pdf", retrieved.getPdfSheetPath());
        assertEquals("/audio/summertime.mp3",    retrieved.getAudioPath());
        assertEquals("https://youtube.com/watch?v=xyz", retrieved.getYoutubeUrl());
        assertEquals("/cover/summertime.jpg",    retrieved.getCoverPath());
        assertEquals("Un classico del jazz americano", retrieved.getDescription());
    }

    @Test
    @DisplayName("Flusso completo: commento su brano → appare in coda moderazione → viene approvato")
    public void testCommentModerationFlow() {
        songDAO.addSong(new Song(0, "Clair de Lune", "Claude Debussy", "Musica classica",
                "", "", "", "", uploaderId, "Lucia", "Upload", "Il famoso pezzo pianistico di Debussy"));
        int songId = songDAO.getAllSongs().stream()
                .filter(s -> "Clair de Lune".equals(s.getTitle()))
                .mapToInt(Song::getId).findFirst().orElse(0);

        commentDAO.addComment(
                new Comment(0, songId, 0, 0, commenterId,
                        "Uno dei brani più belli per pianoforte, difficilissimo da eseguire", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId
        );

        Map<String, List<Comment>> queue = commentDAO.getCommentsToModerate(uploaderId);
        assertFalse(queue.isEmpty(), "La coda di moderazione non deve essere vuota");

        Comment toModerate = queue.values().iterator().next().get(0);
        assertEquals("Uno dei brani più belli per pianoforte, difficilissimo da eseguire",
                toModerate.getContent());

        commentDAO.updateCommentStatus(toModerate.getId(), "Verified");

        assertTrue(commentDAO.getCommentsListByStatus("Verified").stream()
                .anyMatch(x -> x.getId() == toModerate.getId()),
                "Il commento approvato deve essere nella lista Verified");

        boolean stillInQueue = commentDAO.getCommentsToModerate(uploaderId).values().stream()
                .flatMap(List::stream)
                .anyMatch(x -> x.getId() == toModerate.getId());
        assertFalse(stillInQueue, "Il commento approvato non deve più essere in coda");
    }

    @Test
    @DisplayName("Flusso: commento bannato → utente segnalato nel report admin")
    public void testCommentBanAndAdminReportFlow() {
        songDAO.addSong(new Song(0, "Yesterday", "The Beatles", "Rock",
                "", "", "", "", uploaderId, "Lucia", "Upload", ""));
        int songId = songDAO.getAllSongs().stream()
                .filter(s -> "Yesterday".equals(s.getTitle()))
                .mapToInt(Song::getId).findFirst().orElse(0);

        commentDAO.addComment(
                new Comment(0, songId, 0, 0, commenterId,
                        "Contenuto inappropriato da bannare", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId
        );
        int commentId = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId)
                .get(0).getId();

        commentDAO.updateCommentStatus(commentId, "Banned");

        assertTrue(commentDAO.getCommentsListByStatus("Banned").stream()
                .anyMatch(x -> x.getId() == commentId));

        List<User> flaggedUsers = userDAO.getUsersWithReportsAbove(1);
        assertTrue(flaggedUsers.stream().anyMatch(u -> u.getId() == commenterId),
                "L'utente con commenti bannati deve comparire nel report admin");
    }

    @Test
    @DisplayName("Flusso: like su commento → contatore incrementa → double-like bloccato → rimozione like")
    public void testLikeToggleAndRemoveFlow() {
        songDAO.addSong(new Song(0, "Stairway to Heaven", "Led Zeppelin", "Rock",
                "", "", "", "", uploaderId, "Lucia", "Upload", ""));
        int songId = songDAO.getAllSongs().stream()
                .filter(s -> "Stairway to Heaven".equals(s.getTitle()))
                .mapToInt(Song::getId).findFirst().orElse(0);

        commentDAO.addComment(
                new Comment(0, songId, 0, 0, commenterId,
                        "Uno dei riff più famosi della storia del rock", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId
        );
        int commentId = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId)
                .get(0).getId();

        assertFalse(commentDAO.hasUserLiked(uploaderId, commentId));
        commentDAO.toggleLike(uploaderId, commentId);
        assertTrue(commentDAO.hasUserLiked(uploaderId, commentId));
        assertEquals(1, commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId)
                .get(0).getLikes());

        commentDAO.toggleLike(uploaderId, commentId);
        assertEquals(1, commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId)
                .get(0).getLikes(), "Il double-like non deve incrementare il contatore");

        commentDAO.removeLike(uploaderId, commentId);
        assertFalse(commentDAO.hasUserLiked(uploaderId, commentId));
        assertEquals(0, commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId)
                .get(0).getLikes());
    }

    @Test
    @DisplayName("Brani commentati dall'utente visibili nel suo profilo")
    public void testSongsCommentedByUserVisibleInProfile() {
        songDAO.addSong(new Song(0, "Primavera", "Vivaldi", "Musica classica",
                "", "", "", "", uploaderId, "Lucia", "Upload", ""));
        songDAO.addSong(new Song(0, "Autunno", "Vivaldi", "Musica classica",
                "", "", "", "", uploaderId, "Lucia", "Upload", ""));

        List<Song> all = songDAO.getAllSongs();
        int songId1 = all.get(all.size() - 2).getId();
        int songId2 = all.get(all.size() - 1).getId();

        commentDAO.addComment(
                new Comment(0, songId1, 0, 0, commenterId, "La primavera suona magnificamente", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId1
        );
        commentDAO.addComment(
                new Comment(0, songId2, 0, 0, commenterId, "L'autunno è ancora più bella", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId2
        );

        List<Song> commentedSongs = songDAO.getSongsCommentedByUser(commenterId);
        assertEquals(2, commentedSongs.size(), "Il profilo deve mostrare 2 brani commentati");
        assertTrue(commentedSongs.stream().anyMatch(s -> s.getId() == songId1));
        assertTrue(commentedSongs.stream().anyMatch(s -> s.getId() == songId2));
    }

    @Test
    @DisplayName("Commenti su brani diversi non si mescolano - isolamento per resource")
    public void testCommentIsolationBetweenSongs() {
        songDAO.addSong(new Song(0, "Bohemian Rhapsody", "Queen", "Rock",
                "", "", "", "", uploaderId, "Lucia", "Upload", ""));
        songDAO.addSong(new Song(0, "Hotel California", "Eagles", "Rock",
                "", "", "", "", uploaderId, "Lucia", "Upload", ""));

        List<Song> songs = songDAO.getAllSongs();
        int songId1 = songs.get(songs.size() - 2).getId();
        int songId2 = songs.get(songs.size() - 1).getId();

        commentDAO.addComment(
                new Comment(0, songId1, 0, 0, commenterId, "Commento su Bohemian", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId1
        );
        commentDAO.addComment(
                new Comment(0, songId2, 0, 0, commenterId, "Commento su Hotel California", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songId2
        );

        List<Comment> commentsOnSong1 = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId1);
        assertEquals(1, commentsOnSong1.size());
        assertEquals("Commento su Bohemian", commentsOnSong1.get(0).getContent());

        List<Comment> commentsOnSong2 = commentDAO.getCommentsByResource(CommentManager.ResourceType.SONG, songId2);
        assertEquals(1, commentsOnSong2.size());
        assertEquals("Commento su Hotel California", commentsOnSong2.get(0).getContent());
    }
}
