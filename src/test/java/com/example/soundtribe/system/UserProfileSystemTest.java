package com.example.soundtribe.system;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.item.UserSession;
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
@DisplayName("System Test - Flusso Profilo Utente (Aggiornamento, Ricerca e Sessione)")
public class UserProfileSystemTest {

    private UserDAO userDAO;
    private SongDAO songDAO;
    private CommentDAO commentDAO;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        userDAO    = new UserDAO();
        songDAO    = new SongDAO();
        commentDAO = new CommentDAO();
        UserSession.getInstance().cleanUserSession();
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
    @DisplayName("Flusso completo: modifica profilo → login con nuova password → dati aggiornati")
    public void testProfileUpdateAndReloginFlow() {
        userDAO.registerUser(new User("Fabio", "Conti", "fabio.conti@gmail.com", "vecchiapassword", "Rock"));
        User user = userDAO.login("fabio.conti@gmail.com", "vecchiapassword");
        assertNotNull(user);
        int userId = user.getId();

        user.setName("Fabio Junior");
        user.setPassword("nuovapassword");
        user.setFavoriteGenre("Jazz");
        user.setProfilePicPath("/imgs/fabio_new.jpg");
        assertTrue(userDAO.updateUser(user), "L'aggiornamento del profilo deve avere successo");

        assertNull(userDAO.login("fabio.conti@gmail.com", "vecchiapassword"),
                "Il login con la vecchia password deve fallire");

        User loginNew = userDAO.login("fabio.conti@gmail.com", "nuovapassword");
        assertNotNull(loginNew);
        assertEquals("Fabio Junior",        loginNew.getName());
        assertEquals("Jazz",                loginNew.getFavoriteGenre());
        assertEquals("/imgs/fabio_new.jpg", loginNew.getProfilePicPath());
        assertEquals(userId,                loginNew.getId());
    }

    @Test
    @DisplayName("Flusso: ricerca utenti per nome, cognome ed email → solo Verified compaiono")
    public void testUserSearchOnlyShowsVerifiedFlow() {
        userDAO.registerUser(new User("Chiara", "Bianchi", "chiara.bianchi@gmail.com", "p1", "Pop"));
        userDAO.registerUser(new User("Dario",  "Bianchi", "dario.bianchi@gmail.com",  "p2", "Rock"));
        userDAO.registerUser(new User("Eva",    "Rossi",   "eva.rossi@gmail.com",       "p3", "Jazz"));

        userDAO.updateUserStatus(userDAO.login("chiara.bianchi@gmail.com", "p1").getId(), "Verified");
        userDAO.updateUserStatus(userDAO.login("dario.bianchi@gmail.com", "p2").getId(), "Verified");
        // Eva rimane Pending

        List<User> bianchiResults = userDAO.searchUsers("Bianchi");
        assertEquals(2, bianchiResults.size(), "Deve trovare 2 Bianchi verificati");
        assertTrue(bianchiResults.stream().noneMatch(u -> "eva.rossi@gmail.com".equals(u.getEmail())),
                "Eva (Pending) non deve apparire nella ricerca");

        assertEquals(1, userDAO.searchUsers("dario.bianchi").size());
        assertEquals("Dario", userDAO.searchUsers("dario.bianchi").get(0).getName());
    }

    @Test
    @DisplayName("Flusso sessione: profilo caricato da getUserById corrisponde alla sessione attiva")
    public void testSessionMatchesProfileFromDatabase() {
        userDAO.registerUser(new User("Lara", "Croft", "lara.croft@gmail.com", "adventure", "Indie"));
        User user = userDAO.login("lara.croft@gmail.com", "adventure");
        userDAO.updateUserStatus(user.getId(), "Verified");

        User loggedIn = userDAO.login("lara.croft@gmail.com", "adventure");
        assertNotNull(loggedIn);
        UserSession session = UserSession.getInstance();
        session.setUserId(loggedIn.getId());
        session.setIsAdmin(loggedIn.isAdmin());
        session.setLastSearchQuery("chitarra jazz");

        User fromDB = userDAO.getUserById(session.getUserId());
        assertNotNull(fromDB);
        assertEquals("Lara",     fromDB.getName());
        assertEquals("Croft",    fromDB.getSurname());
        assertEquals("Verified", fromDB.getStatus());
        assertFalse(fromDB.isAdmin());
        assertEquals("chitarra jazz", session.getLastSearchQuery());
    }

    @Test
    @DisplayName("Flusso: profilo utente mostra solo i brani che ha commentato")
    public void testProfileShowsCommentedSongsOnly() {
        userDAO.registerUser(new User("Uploader", "Test", "uploader@gmail.com", "pw1", "Rock"));
        userDAO.registerUser(new User("Commenter", "Test", "commenter@gmail.com", "pw2", "Pop"));
        int upId  = userDAO.login("uploader@gmail.com", "pw1").getId();
        int comId = userDAO.login("commenter@gmail.com", "pw2").getId();

        songDAO.addSong(new Song(0, "Brano Alpha", "Artista X", "Rock",
                "", "", "", "", upId, "Uploader", "Test", ""));
        songDAO.addSong(new Song(0, "Brano Beta", "Artista X", "Pop",
                "", "", "", "", upId, "Uploader", "Test", ""));

        List<Song> allSongs = songDAO.getAllSongs();
        int songAlpha = allSongs.get(allSongs.size() - 2).getId();
        int songBeta  = allSongs.get(allSongs.size() - 1).getId();

        // Commenta solo il primo brano
        commentDAO.addComment(
                new Comment(0, songAlpha, 0, 0, comId, "Bel brano!", 0, null, "Pending"),
                CommentManager.ResourceType.SONG, songAlpha
        );

        List<Song> commentedSongs = songDAO.getSongsCommentedByUser(comId);
        assertEquals(1, commentedSongs.size());
        assertEquals(songAlpha, commentedSongs.get(0).getId());
        assertFalse(commentedSongs.stream().anyMatch(s -> s.getId() == songBeta));
    }

    @Test
    @DisplayName("Flusso login/logout multipli con sessione sempre coerente")
    public void testMultipleLoginLogoutSessionCoherenceFlow() {
        userDAO.registerUser(new User("Utente1", "Uno", "utente1@gmail.com", "pw1", "Rock"));
        userDAO.registerUser(new User("Utente2", "Due", "utente2@gmail.com", "pw2", "Jazz"));
        int id1 = userDAO.login("utente1@gmail.com", "pw1").getId();
        int id2 = userDAO.login("utente2@gmail.com", "pw2").getId();

        UserSession session = UserSession.getInstance();

        session.setUserId(id1);
        session.setIsAdmin(false);
        assertEquals(id1, session.getUserId());

        session.cleanUserSession();
        assertEquals(0, session.getUserId());

        session.setUserId(id2);
        assertEquals(id2, session.getUserId());
        assertNotEquals(id1, session.getUserId());

        session.cleanUserSession();
        assertEquals(0, session.getUserId());
        assertFalse(session.isAdmin());
        assertNull(session.getLastSearchQuery());
    }
}
