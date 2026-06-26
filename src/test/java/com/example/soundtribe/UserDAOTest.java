package com.example.soundtribe;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.CredDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test UserDAO - Autenticazione e Registrazione")
public class UserDAOTest {

    private UserDAO userDAO;
    private static final String PASSWORD = "soundtribe123";

    @BeforeEach
    public void setUp() {
        resetDatabase();
        userDAO = new UserDAO();
        // Popola il DB con 10 utenti fittizi, ognuno con una canzone e un commento
        TestDataSeeder.seed(userDAO, new SongDAO(), new CommentDAO());
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
    @DisplayName("Registrazione utente con successo")
    public void testRegisterUserSuccess() {
        User newUser = new User("Marco", "Rossi", "marco.rossi@gmail.com", PASSWORD, "Rock");
        newUser.setMotivation("Suono la chitarra da 5 anni e voglio condividere le mie composizioni");

        boolean success = userDAO.registerUser(newUser);

        assertTrue(success, "La registrazione dovrebbe avere successo");
    }

    @Test
    @DisplayName("Email duplicata - registrazione fallisce")
    public void testRegisterUserDuplicateEmail() {
        User user1 = new User("Mario", "Bianchi", "mario.bianchi@gmail.com", PASSWORD, "Jazz");
        User user2 = new User("Luigi", "Verdi",   "mario.bianchi@gmail.com", PASSWORD, "Pop");

        boolean success1 = userDAO.registerUser(user1);
        boolean success2 = userDAO.registerUser(user2);

        assertTrue(success1,  "Primo utente dovrebbe registrarsi");
        assertFalse(success2, "Secondo utente con email duplicata dovrebbe fallire");
    }

    @Test
    @DisplayName("Login con credenziali corrette")
    public void testLoginSuccess() {
        userDAO.registerUser(new User("Anna", "Ferretti", "anna.ferretti@gmail.com", PASSWORD, "Blues"));

        User loggedUser = userDAO.login("anna.ferretti@gmail.com", PASSWORD);

        assertNotNull(loggedUser, "Login dovrebbe ritornare un utente");
        assertEquals("Anna", loggedUser.getName());
        assertEquals("anna.ferretti@gmail.com", loggedUser.getEmail());
    }

    @Test
    @DisplayName("Login con password errata")
    public void testLoginFailPassword() {
        userDAO.registerUser(new User("Giovanni", "Russo", "giovanni.russo@gmail.com", PASSWORD, "Rock"));

        User loggedUser = userDAO.login("giovanni.russo@gmail.com", "passwordsbagliata");

        assertNull(loggedUser, "Login con password errata dovrebbe ritornare null");
    }

    @Test
    @DisplayName("Login con email non esistente")
    public void testLoginFailEmail() {
        User loggedUser = userDAO.login("utente.inesistente@gmail.com", PASSWORD);

        assertNull(loggedUser, "Login con email inesistente dovrebbe ritornare null");
    }

    @Test
    @DisplayName("Recupero utente per ID")
    public void testGetUserById() {
        userDAO.registerUser(new User("Lucia", "Marino", "lucia.marino@gmail.com", PASSWORD, "Pop"));

        User found = userDAO.login("lucia.marino@gmail.com", PASSWORD);
        assertNotNull(found);

        User byId = userDAO.getUserById(found.getId());
        assertNotNull(byId, "Recupero per ID dovrebbe trovare l'utente");
        assertEquals("Lucia", byId.getName());
    }

    @Test
    @DisplayName("Approvazione utente - status diventa Verified")
    public void testUpdateUserStatus() {
        userDAO.registerUser(new User("Paolo", "Gallo", "paolo.gallo@gmail.com", PASSWORD, "Indie"));

        User user = userDAO.login("paolo.gallo@gmail.com", PASSWORD);
        userDAO.updateUserStatus(user.getId(), "Verified");

        User updated = userDAO.getUserById(user.getId());
        assertEquals("Verified", updated.getStatus(), "Status dovrebbe essere Verified");
    }

    @Test
    @DisplayName("Ban utente con motivazione salvata")
    public void testBanUser() {
        userDAO.registerUser(new User("Filippo", "Costa", "filippo.costa@gmail.com", PASSWORD, "Metal"));

        User user = userDAO.login("filippo.costa@gmail.com", PASSWORD);
        String motivo = "Commenti offensivi ripetuti verso altri utenti";
        userDAO.updateUserStatus(user.getId(), "Banned", motivo);

        User bannedUser = userDAO.getUserById(user.getId());
        assertEquals("Banned", bannedUser.getStatus());
        assertEquals(motivo, bannedUser.getMotivation(), "Motivo del ban dovrebbe essere salvato");
    }

    @Test
    @DisplayName("getAllUsers ritorna solo utenti Verified")
    public void testGetAllUsersFiltered() {
        java.util.List<User> verifiedUsers = userDAO.getAllUsers();

        assertNotNull(verifiedUsers);
        for (User u : verifiedUsers) {
            assertEquals("Verified", u.getStatus(), "Tutti gli utenti dovrebbero essere Verified");
        }
    }

    @Test
    @DisplayName("getPendingUsers ritorna solo utenti in attesa di approvazione")
    public void testGetPendingUsers() {
        java.util.List<User> pendingUsers = userDAO.getPendingUsers();

        assertNotNull(pendingUsers);
        assertFalse(pendingUsers.isEmpty(), "Dovrebbero esserci utenti in attesa (i 10 utenti fittizi)");
        for (User u : pendingUsers) {
            assertEquals("Pending", u.getStatus(), "Tutti gli utenti dovrebbero essere Pending");
        }
    }
}
