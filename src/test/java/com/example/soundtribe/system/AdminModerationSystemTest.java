package com.example.soundtribe.system;

import com.example.soundtribe.TestDataSeeder;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.User;
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
@DisplayName("System Test - Pannello di amministrazione e moderazione")
public class AdminModerationSystemTest {

    private UserDAO userDAO;
    private SongDAO songDAO;
    private CommentDAO commentDAO;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        userDAO    = new UserDAO();
        songDAO    = new SongDAO();
        commentDAO = new CommentDAO();
        TestDataSeeder.seed(userDAO, songDAO, commentDAO);
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
    @DisplayName("Dashboard admin: statistiche corrette dopo il seeding")
    public void testAdminDashboardStats() {
        assertEquals(11, userDAO.getNumberUsers(), "Il totale deve essere 11 (10 seed + 1 admin)");
        assertEquals(10, userDAO.getNumberUsersByStatus("Pending"));
        assertEquals(1,  userDAO.getNumberUsersByStatus("Verified"));
        assertEquals(10, commentDAO.getTotalComments());
        assertEquals(10, commentDAO.getCommentsByStatus("Pending"));
    }

    @Test
    @DisplayName("Admin approva metà degli utenti e banna gli altri - contatori corretti")
    public void testAdminBulkUserModerationFlow() {
        List<User> pending = userDAO.getPendingUsers();
        assertEquals(10, pending.size());

        for (int i = 0; i < 5; i++) {
            userDAO.updateUserStatus(pending.get(i).getId(), "Verified");
        }
        for (int i = 5; i < 10; i++) {
            userDAO.updateUserStatus(pending.get(i).getId(), "Banned", "Ban di massa per test");
        }

        assertEquals(0, userDAO.getNumberUsersByStatus("Pending"));
        assertEquals(6, userDAO.getNumberUsersByStatus("Verified")); // 5 approvati + admin
        assertEquals(5, userDAO.getNumberUsersByStatus("Banned"));
    }

    @Test
    @DisplayName("Admin banna commenti e identifica gli utenti problematici")
    public void testAdminBanCommentsAndFlagUsersFlow() {
        List<Comment> pendingComments = commentDAO.getCommentsListByStatus("Pending");
        assertTrue(pendingComments.size() >= 3);

        for (int i = 0; i < 3; i++) {
            commentDAO.updateCommentStatus(pendingComments.get(i).getId(), "Banned");
        }

        assertEquals(3, commentDAO.getCommentsByStatus("Banned"));
        assertEquals(7, commentDAO.getCommentsByStatus("Pending"));

        // Nel seed ogni utente ha al massimo 1 commento → 3 utenti flaggati
        List<User> flagged = userDAO.getUsersWithReportsAbove(1);
        assertEquals(3, flagged.size(), "Devono esserci 3 utenti con almeno un commento bannato");
    }

    @Test
    @DisplayName("Admin verifica tutti i commenti → lista Pending svuotata, lista Verified popolata")
    public void testAdminVerifyAllCommentsFlow() {
        for (Comment c : commentDAO.getCommentsListByStatus("Pending")) {
            commentDAO.updateCommentStatus(c.getId(), "Verified");
        }

        assertEquals(0,  commentDAO.getCommentsByStatus("Pending"));
        assertEquals(10, commentDAO.getCommentsByStatus("Verified"));
    }

    @Test
    @DisplayName("Admin approva utenti → appaiono nella ricerca per nome")
    public void testApprovedUsersSearchableFlow() {
        List<User> pending = userDAO.getPendingUsers();

        // Approva i primi 3 (Luca Ricci, Sara Conti, Marco Bianchi dal seed)
        for (int i = 0; i < 3; i++) {
            userDAO.updateUserStatus(pending.get(i).getId(), "Verified");
        }

        List<User> ricciResults = userDAO.searchUsers("Ricci");
        assertFalse(ricciResults.isEmpty(), "Luca Ricci deve comparire nella ricerca dopo l'approvazione");

        for (User u : userDAO.getAllUsers()) {
            assertEquals("Verified", u.getStatus(), "La ricerca deve restituire solo utenti Verified");
        }
    }

    @Test
    @DisplayName("Admin ottiene la motivazione del ban per ogni utente bannato")
    public void testBanReasonPersistsInAdminView() {
        User target = userDAO.getPendingUsers().get(0);
        String ragione = "Inserimento di dati falsi nella richiesta di accesso";
        userDAO.updateUserStatus(target.getId(), "Banned", ragione);

        User bannedUser = userDAO.getUserById(target.getId());
        assertEquals("Banned", bannedUser.getStatus());
        assertEquals(ragione, bannedUser.getMotivation(),
                "La motivazione del ban deve essere recuperabile dall'admin");
    }
}
