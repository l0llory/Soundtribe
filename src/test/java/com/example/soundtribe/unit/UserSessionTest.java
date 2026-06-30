package com.example.soundtribe.unit;

import com.example.soundtribe.item.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("Test UserSession - Pattern Singleton")
public class UserSessionTest {

    @BeforeEach
    public void setUp() {
        UserSession.getInstance().cleanUserSession();
    }

    @Test
    @DisplayName("Singleton - una sola istanza")
    public void testSingletonInstance() {
        UserSession a = UserSession.getInstance();
        UserSession b = UserSession.getInstance();

        assertSame(a, b, "Dovrebbe essere sempre la stessa istanza");
    }

    @Test
    @DisplayName("Impostazione e recupero dell'ID utente")
    public void testSetAndGetUserId() {
        UserSession session = UserSession.getInstance();
        session.setUserId(42);

        assertEquals(42, session.getUserId());
    }

    @Test
    @DisplayName("Verifica privilegi admin")
    public void testAdminPrivileges() {
        UserSession session = UserSession.getInstance();

        assertFalse(session.isAdmin(), "All'avvio non dovrebbe essere admin");

        session.setIsAdmin(true);
        assertTrue(session.isAdmin());

        session.setIsAdmin(false);
        assertFalse(session.isAdmin());
    }

    @Test
    @DisplayName("I dati persistono nella stessa sessione")
    public void testSessionPersistence() {
        UserSession.getInstance().setUserId(123);
        UserSession.getInstance().setIsAdmin(true);

        assertEquals(123, UserSession.getInstance().getUserId());
        assertTrue(UserSession.getInstance().isAdmin());
    }

    @Test
    @DisplayName("Pulizia della sessione al logout")
    public void testCleanUserSession() {
        UserSession session = UserSession.getInstance();
        session.setUserId(999);
        session.setIsAdmin(true);

        session.cleanUserSession();

        assertEquals(0, session.getUserId(), "L'ID dovrebbe essere resettato");
        assertFalse(session.isAdmin(), "Lo status admin dovrebbe essere resettato");
    }

    @Test
    @DisplayName("Query di ricerca salvata in sessione")
    public void testLastSearchQuery() {
        UserSession session = UserSession.getInstance();

        session.setLastSearchQuery("De André");
        assertEquals("De André", session.getLastSearchQuery());

        session.setLastSearchQuery(null);
        assertNull(session.getLastSearchQuery());
    }

    @Test
    @DisplayName("Sessione vuota prima del login")
    public void testSessionStartsEmpty() {
        UserSession session = UserSession.getInstance();

        assertEquals(0, session.getUserId(), "Prima del login l'ID deve essere 0");
        assertFalse(session.isAdmin(), "Prima del login non si è admin");
        assertNull(session.getLastSearchQuery(), "Prima del login non ci sono ricerche salvate");
    }

    @Test
    @DisplayName("Simulazione login e logout")
    public void testLoginLogoutFlow() {
        UserSession session = UserSession.getInstance();

        session.setUserId(50);
        session.setIsAdmin(false);

        assertTrue(session.getUserId() > 0);
        assertFalse(session.isAdmin());

        session.cleanUserSession();

        assertEquals(0, session.getUserId());
        assertFalse(session.isAdmin());
    }

    @Test
    @DisplayName("Simulazione login come amministratore")
    public void testAdminLoginFlow() {
        UserSession session = UserSession.getInstance();

        session.setUserId(1);
        session.setIsAdmin(true);

        assertTrue(session.isAdmin());
        assertEquals(1, session.getUserId());

        session.cleanUserSession();

        assertFalse(session.isAdmin(), "Dopo logout l'admin non deve rimanere attivo");
        assertEquals(0, session.getUserId());
    }
}
