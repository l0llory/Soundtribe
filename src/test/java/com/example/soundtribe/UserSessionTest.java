package com.example.soundtribe;

import com.example.soundtribe.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test UserSession - Pattern Singleton")
public class UserSessionTest {

    @BeforeEach
    public void setUp() {
        // Pulisci la sessione prima di ogni test
        UserSession.getInstance().cleanUserSession();
    }

    @Test
    @DisplayName("Singleton - una sola istanza")
    public void testSingletonInstance() {
        UserSession instance1 = UserSession.getInstance();
        UserSession instance2 = UserSession.getInstance();
        UserSession instance3 = UserSession.getInstance();

        assertSame(instance1, instance2, "Le istanze dovrebbero essere le stesse");
        assertSame(instance2, instance3, "Le istanze dovrebbero essere le stesse");
    }

    @Test
    @DisplayName("Impostazione e recupero dell'ID utente")
    public void testSetAndGetUserId() {
        UserSession session = UserSession.getInstance();
        int testId = 42;

        session.setUserId(testId);

        assertEquals(testId, session.getUserId(), "L'ID dovrebbe essere quello impostato");
    }

    @Test
    @DisplayName("Impostazione e verifica privilegi admin")
    public void testAdminPrivileges() {
        UserSession session = UserSession.getInstance();

        assertFalse(session.isAdmin(), "Inizialmente non dovrebbe essere admin");

        session.setIsAdmin(true);
        assertTrue(session.isAdmin(), "Dovrebbe essere admin dopo setIsAdmin(true)");

        session.setIsAdmin(false);
        assertFalse(session.isAdmin(), "Non dovrebbe essere admin dopo setIsAdmin(false)");
    }

    @Test
    @DisplayName("Persistenza dati nella sessione")
    public void testSessionPersistence() {
        UserSession session = UserSession.getInstance();

        session.setUserId(123);
        session.setIsAdmin(true);

        // Recupera la stessa istanza
        UserSession sameSession = UserSession.getInstance();

        assertEquals(123, sameSession.getUserId(), "L'ID dovrebbe persistere");
        assertTrue(sameSession.isAdmin(), "Lo status admin dovrebbe persistere");
    }

    @Test
    @DisplayName("Pulizia della sessione")
    public void testCleanUserSession() {
        UserSession session = UserSession.getInstance();

        session.setUserId(999);
        session.setIsAdmin(true);

        assertEquals(999, session.getUserId());
        assertTrue(session.isAdmin());

        session.cleanUserSession();

        assertEquals(0, session.getUserId(), "L'ID dovrebbe essere resettato a 0");
        assertFalse(session.isAdmin(), "Lo status admin dovrebbe essere resettato a false");
    }

    @Test
    @DisplayName("Ricerca salvata in sessione")
    public void testLastSearchQuery() {
        UserSession session = UserSession.getInstance();

        String testQuery = "Bohemian Rhapsody";
        session.setLastSearchQuery(testQuery);

        assertEquals(testQuery, session.getLastSearchQuery(), "La query dovrebbe essere salvata");

        session.setLastSearchQuery(null);
        assertNull(session.getLastSearchQuery(), "La query dovrebbe essere null dopo pulizia");
    }

    @Test
    @DisplayName("Stato admin indipendente dall'ID utente")
    public void testAdminStateIndependence() {
        UserSession session = UserSession.getInstance();

        session.setUserId(100);
        session.setIsAdmin(true);

        UserSession sameSession = UserSession.getInstance();
        sameSession.setUserId(101);

        // Lo stato admin dovrebbe rimanere true
        assertTrue(sameSession.isAdmin(), "Lo status admin dovrebbe rimanere invariato");
        assertEquals(101, sameSession.getUserId(), "L'ID dovrebbe essere aggiornato");
    }

    @Test
    @DisplayName("Thread-safe getInstance (double-checked locking)")
    public void testThreadSafeSingleton() throws InterruptedException {
        // Questo test verifica che il Singleton sia thread-safe
        UserSession[] instances = new UserSession[5];

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = UserSession.getInstance();
            });
        }

        // Avvia tutti i thread
        for (Thread t : threads) {
            t.start();
        }

        // Aspetta che tutti i thread finiscano
        for (Thread t : threads) {
            t.join();
        }

        // Verifica che tutte le istanze siano la stessa
        UserSession firstInstance = instances[0];
        for (int i = 1; i < instances.length; i++) {
            assertSame(firstInstance, instances[i],
                    "Tutte le istanze da thread diversi dovrebbero essere la stessa");
        }
    }

    @Test
    @DisplayName("Simulazione login e logout")
    public void testLoginLogoutFlow() {
        UserSession session = UserSession.getInstance();

        // Simulazione login
        session.setUserId(50);
        session.setIsAdmin(false);

        assertTrue(session.getUserId() > 0, "Dopo login, dovrebbe avere un ID");
        assertFalse(session.isAdmin(), "Utente normale non dovrebbe essere admin");

        // Simulazione logout
        session.cleanUserSession();

        assertEquals(0, session.getUserId(), "Dopo logout, l'ID dovrebbe essere 0");
        assertFalse(session.isAdmin(), "Dopo logout, non dovrebbe essere admin");
    }

    @Test
    @DisplayName("Simulazione cambio utente")
    public void testUserSwitch() {
        UserSession session = UserSession.getInstance();

        // Primo utente
        session.setUserId(1);
        session.setIsAdmin(false);
        assertEquals(1, session.getUserId());

        // Cambio a secondo utente
        session.setUserId(2);
        session.setIsAdmin(true);
        assertEquals(2, session.getUserId());
        assertTrue(session.isAdmin());

        // Verifica che il cambio sia avvenuto
        UserSession sameSession = UserSession.getInstance();
        assertEquals(2, sameSession.getUserId());
        assertTrue(sameSession.isAdmin());
    }

    @Test
    @DisplayName("Ricerca e pulizia della query")
    public void testSearchQueryCleanup() {
        UserSession session = UserSession.getInstance();

        // Imposta una ricerca
        session.setLastSearchQuery("filter:commented_by_me");
        assertNotNull(session.getLastSearchQuery());

        // Simula il consumo della ricerca
        String query = session.getLastSearchQuery();
        assertEquals("filter:commented_by_me", query);

        // Pulizia dopo utilizzo
        session.setLastSearchQuery(null);
        assertNull(session.getLastSearchQuery());
    }
}