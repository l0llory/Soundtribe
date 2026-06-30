package com.example.soundtribe;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;

/**
 * Runner standalone per popolare il database con dati di test
 * SENZA truncare le tabelle.
 *
 * Eseguire come Java Application dall'IDE (tasto destro → Run As → Java Application)
 * oppure con: mvn exec:java -Dexec.mainClass="com.example.soundtribe.TestDataSeederRunner"
 *
 * Nota: se i dati del seed sono già presenti, le email duplicate verranno
 * scartate silenziosamente (constraint UNIQUE su email).
 */
public class TestDataSeederRunner {

    public static void main(String[] args) {
        System.out.println("=== TestDataSeeder: avvio popolamento database ===");

        UserDAO    userDAO    = new UserDAO();
        SongDAO    songDAO    = new SongDAO();
        CommentDAO commentDAO = new CommentDAO();

        int[] userIds = TestDataSeeder.seed(userDAO, songDAO, commentDAO);

        System.out.println("Seeding completato:");
        System.out.println("  - " + userIds.length + " utenti inseriti");
        System.out.println("  - " + userIds.length + " canzoni inserite (1 per utente)");
        System.out.println("  - " + userIds.length + " commenti inseriti (schema circolare)");
        System.out.println("=== Fine seeding ===");
    }
}
