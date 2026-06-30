package com.example.soundtribe.system;

import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.item.UserSession;
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
@DisplayName("System Test - Flusso Registrazione, Approvazione e Login")
public class RegistrationApprovalSystemTest {

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        resetDatabase();
        userDAO = new UserDAO();
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
    @DisplayName("Flusso completo: registrazione → approvazione admin → login")
    public void testRegistrationApprovalLoginFlow() {
        User newUser = new User("Elena", "Bassi", "elena.bassi@gmail.com", "password123", "Jazz");
        newUser.setMotivation("Voglio condividere la mia musica con la comunità");
        assertTrue(userDAO.registerUser(newUser), "La registrazione deve avere successo");

        List<User> pending = userDAO.getPendingUsers();
        assertTrue(pending.stream().anyMatch(u -> "elena.bassi@gmail.com".equals(u.getEmail())),
                "L'utente deve comparire nella lista Pending");

        assertFalse(userDAO.getAllUsers().stream().anyMatch(u -> "elena.bassi@gmail.com".equals(u.getEmail())),
                "L'utente non deve ancora essere tra i Verified");

        User pendingUser = userDAO.login("elena.bassi@gmail.com", "password123");
        assertNotNull(pendingUser);
        userDAO.updateUserStatus(pendingUser.getId(), "Verified");

        assertTrue(userDAO.getAllUsers().stream().anyMatch(u -> "elena.bassi@gmail.com".equals(u.getEmail())),
                "Dopo l'approvazione l'utente deve essere tra i Verified");
        assertFalse(userDAO.getPendingUsers().stream().anyMatch(u -> "elena.bassi@gmail.com".equals(u.getEmail())),
                "Dopo l'approvazione l'utente non deve più essere tra i Pending");
    }

    @Test
    @DisplayName("Flusso completo: registrazione → ban con motivazione")
    public void testRegistrationBanWithReasonFlow() {
        userDAO.registerUser(new User("Cattivo", "Utente", "cattivo@gmail.com", "pass123", "Rock"));

        User user = userDAO.login("cattivo@gmail.com", "pass123");
        assertNotNull(user);
        String motivo = "Comportamento offensivo durante la fase di registrazione";
        userDAO.updateUserStatus(user.getId(), "Banned", motivo);

        User banned = userDAO.getUserById(user.getId());
        assertEquals("Banned", banned.getStatus());
        assertEquals(motivo, banned.getMotivation());

        assertFalse(userDAO.getAllUsers().stream().anyMatch(u -> u.getId() == user.getId()));
        assertFalse(userDAO.getPendingUsers().stream().anyMatch(u -> u.getId() == user.getId()));
    }

    @Test
    @DisplayName("Sessione utente aggiornata correttamente dopo il login")
    public void testSessionSetAfterLogin() {
        userDAO.registerUser(new User("Marco", "Verdi", "marco.verdi@gmail.com", "abc123", "Pop"));
        User registered = userDAO.login("marco.verdi@gmail.com", "abc123");
        userDAO.updateUserStatus(registered.getId(), "Verified");

        User loggedIn = userDAO.login("marco.verdi@gmail.com", "abc123");
        assertNotNull(loggedIn);
        UserSession session = UserSession.getInstance();
        session.setUserId(loggedIn.getId());
        session.setIsAdmin(loggedIn.isAdmin());

        assertEquals(loggedIn.getId(), session.getUserId());
        assertFalse(session.isAdmin(), "Un utente normale non deve essere admin");

        session.cleanUserSession();
        assertEquals(0, session.getUserId());
        assertFalse(session.isAdmin());
    }

    @Test
    @DisplayName("Admin può loggarsi e la sessione riflette i privilegi")
    public void testAdminLoginSetsSessionCorrectly() {
        User admin = userDAO.login("admin@soundtribe.it", "admin");
        assertNotNull(admin, "L'admin deve poter fare login");
        assertTrue(admin.isAdmin(), "L'admin deve avere is_admin = true");

        UserSession session = UserSession.getInstance();
        session.setUserId(admin.getId());
        session.setIsAdmin(admin.isAdmin());

        assertTrue(session.isAdmin());
        assertEquals(admin.getId(), session.getUserId());
    }

    @Test
    @DisplayName("Contatori utenti aggiornati correttamente dopo approvazioni e ban")
    public void testUserCountersByStatus() {
        userDAO.registerUser(new User("Alice", "Rossi", "alice@gmail.com", "p1", "Pop"));
        userDAO.registerUser(new User("Bob",   "Neri",  "bob@gmail.com",   "p2", "Rock"));
        userDAO.registerUser(new User("Carlo", "Bianchi", "carlo@gmail.com", "p3", "Jazz"));

        assertEquals(3, userDAO.getNumberUsersByStatus("Pending"));

        userDAO.updateUserStatus(userDAO.login("alice@gmail.com", "p1").getId(), "Verified");
        userDAO.updateUserStatus(userDAO.login("bob@gmail.com", "p2").getId(), "Verified");
        userDAO.updateUserStatus(userDAO.login("carlo@gmail.com", "p3").getId(), "Banned", "Test ban");

        assertEquals(0, userDAO.getNumberUsersByStatus("Pending"));
        assertEquals(3, userDAO.getNumberUsersByStatus("Verified")); // alice + bob + admin
        assertEquals(1, userDAO.getNumberUsersByStatus("Banned"));
    }

    @Test
    @DisplayName("Email duplicata blocca la seconda registrazione, la prima resta valida")
    public void testDuplicateEmailDoesNotBreakExistingUser() {
        userDAO.registerUser(new User("Primo", "Utente", "unico@gmail.com", "pw1", "Rock"));
        boolean secondReg = userDAO.registerUser(new User("Secondo", "Utente", "unico@gmail.com", "pw2", "Pop"));

        assertFalse(secondReg, "La registrazione con email duplicata deve fallire");

        User first = userDAO.login("unico@gmail.com", "pw1");
        assertNotNull(first, "Il primo utente deve poter ancora fare login");
        assertEquals("Primo", first.getName());
    }
}
