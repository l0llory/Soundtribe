package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test UserDAO - Autenticazione e Registrazione")
public class UserDAOTest {

    private UserDAO userDAO;
    private static final String TEST_EMAIL = "testuser_" + System.currentTimeMillis() + "@test.com";
    private static final String TEST_PASSWORD = "TestPassword123";

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
    }

    @Test
    @DisplayName("Registrazione utente con successo")
    public void testRegisterUserSuccess() {
        User newUser = new User("Marco", "Rossi", TEST_EMAIL, TEST_PASSWORD, "Rock");
        newUser.setMotivation("Voglio caricare i miei brani musicali");

        boolean success = userDAO.registerUser(newUser);

        assertTrue(success, "La registrazione dovrebbe avere successo");
    }

    @Test
    @DisplayName("Email duplicata - registrazione fallisce")
    public void testRegisterUserDuplicateEmail() {
        User user1 = new User("Mario", "Bianchi", TEST_EMAIL + "_dup", TEST_PASSWORD, "Jazz");
        User user2 = new User("Luigi", "Verdi", TEST_EMAIL + "_dup", TEST_PASSWORD, "Pop");

        boolean success1 = userDAO.registerUser(user1);
        boolean success2 = userDAO.registerUser(user2);

        assertTrue(success1, "Primo utente dovrebbe registrarsi");
        assertFalse(success2, "Secondo utente con email duplicata dovrebbe fallire");
    }

    @Test
    @DisplayName("Login con credenziali corrette")
    public void testLoginSuccess() {
        // Setup: registra prima un utente
        User testUser = new User("Anna", "Neri", TEST_EMAIL + "_login", TEST_PASSWORD, "Blues");
        userDAO.registerUser(testUser);

        // Test: login
        User loggedUser = userDAO.login(TEST_EMAIL + "_login", TEST_PASSWORD);

        assertNotNull(loggedUser, "Login dovrebbe ritornare un utente");
        assertEquals("Anna", loggedUser.getName());
        assertEquals(TEST_EMAIL + "_login", loggedUser.getEmail());
    }

    @Test
    @DisplayName("Login con password errata")
    public void testLoginFailPassword() {
        User testUser = new User("Giovanni", "Russo", TEST_EMAIL + "_fail", TEST_PASSWORD, "Rock");
        userDAO.registerUser(testUser);

        User loggedUser = userDAO.login(TEST_EMAIL + "_fail", "WrongPassword");

        assertNull(loggedUser, "Login con password errata dovrebbe ritornare null");
    }

    @Test
    @DisplayName("Login con email non esistente")
    public void testLoginFailEmail() {
        User loggedUser = userDAO.login("nonexistent@test.com", TEST_PASSWORD);

        assertNull(loggedUser, "Login con email inesistente dovrebbe ritornare null");
    }

    @Test
    @DisplayName("Recupero utente per ID")
    public void testGetUserById() {
        User newUser = new User("Lucia", "Ferrari", TEST_EMAIL + "_getid", TEST_PASSWORD, "Pop");
        userDAO.registerUser(newUser);

        User foundUser = userDAO.login(TEST_EMAIL + "_getid", TEST_PASSWORD);
        assertNotNull(foundUser, "Utente dovrebbe essere trovato");

        User retrievedUser = userDAO.getUserById(foundUser.getId());
        assertNotNull(retrievedUser, "Recupero per ID dovrebbe trovare l'utente");
        assertEquals("Lucia", retrievedUser.getName());
    }

    @Test
    @DisplayName("Aggiornamento status utente a Verified")
    public void testUpdateUserStatus() {
        User newUser = new User("Paolo", "Gallo", TEST_EMAIL + "_status", TEST_PASSWORD, "Indie");
        userDAO.registerUser(newUser);

        User user = userDAO.login(TEST_EMAIL + "_status", TEST_PASSWORD);
        userDAO.updateUserStatus(user.getId(), "Verified");

        User updatedUser = userDAO.getUserById(user.getId());
        assertEquals("Verified", updatedUser.getStatus(), "Status dovrebbe essere Verified");
    }

    @Test
    @DisplayName("Bannaggio utente con motivo")
    public void testBanUser() {
        User newUser = new User("Filippo", "Costa", TEST_EMAIL + "_ban", TEST_PASSWORD, "Metal");
        userDAO.registerUser(newUser);

        User user = userDAO.login(TEST_EMAIL + "_ban", TEST_PASSWORD);
        String banReason = "Violazione dei termini di servizio";
        userDAO.updateUserStatus(user.getId(), "Banned", banReason);

        User bannedUser = userDAO.getUserById(user.getId());
        assertEquals("Banned", bannedUser.getStatus(), "Status dovrebbe essere Banned");
        assertEquals(banReason, bannedUser.getMotivation(), "Motivo del ban dovrebbe essere salvato");
    }

    @Test
    @DisplayName("getAllUsers ritorna solo utenti Verified")
    public void testGetAllUsersFiltered() {
        // La lista dovrebbe contenere solo utenti con status "Verified"
        java.util.List<User> verifiedUsers = userDAO.getAllUsers();

        assertNotNull(verifiedUsers, "La lista non dovrebbe essere null");
        // Verifica che non ci siano utenti Pending o Banned
        for (User u : verifiedUsers) {
            assertEquals("Verified", u.getStatus(), "Tutti gli utenti dovrebbero essere Verified");
        }
    }

    @Test
    @DisplayName("getPendingUsers ritorna solo utenti Pending")
    public void testGetPendingUsers() {
        java.util.List<User> pendingUsers = userDAO.getPendingUsers();

        assertNotNull(pendingUsers, "La lista non dovrebbe essere null");
        for (User u : pendingUsers) {
            assertEquals("Pending", u.getStatus(), "Tutti gli utenti dovrebbero essere Pending");
        }
    }
}