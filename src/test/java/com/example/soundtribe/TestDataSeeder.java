package com.example.soundtribe;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.manager.CommentManager;

import java.util.List;

/**
 * Popola il database di test con 10 utenti fittizi, ognuno con una canzone caricata
 * (uploader_id = ID reale dell'utente) e un commento sulla canzone dell'utente
 * successivo secondo uno schema circolare.
 *
 * Schema: utente[i] commenta canzone[i+1 % 10]
 * Così ogni commento è legato a un commento E a un utente realmente presenti nel DB.
 *
 * Email con dominio @test.it per non collidere con le email @gmail.com
 * usate dai singoli metodi di test.
 */
public class TestDataSeeder {

    private static final String[][] USERS = {
        {"Luca",     "Ricci",    "luca.ricci@test.it",      "soundtribe123", "Rock"},
        {"Sara",     "Conti",    "sara.conti@test.it",      "soundtribe123", "Jazz"},
        {"Marco",    "Bianchi",  "marco.bianchi@test.it",   "soundtribe123", "Pop"},
        {"Anna",     "Mancini",  "anna.mancini@test.it",    "soundtribe123", "Blues"},
        {"Giovanni", "Ferrari",  "giovanni.ferrari@test.it","soundtribe123", "Folk"},
        {"Lucia",    "Bruno",    "lucia.bruno@test.it",     "soundtribe123", "Indie"},
        {"Paolo",    "Esposito", "paolo.esposito@test.it",  "soundtribe123", "Rock"},
        {"Filippo",  "Romano",   "filippo.romano@test.it",  "soundtribe123", "Metal"},
        {"Elena",    "Greco",    "elena.greco@test.it",     "soundtribe123", "Jazz"},
        {"Roberto",  "Neri",     "roberto.neri@test.it",    "soundtribe123", "Pop"}
    };

    private static final String[][] SONGS = {
        {"Nel blu dipinto di blu",  "Domenico Modugno",      "Pop"},
        {"Piazza Grande",           "Lucio Dalla",           "Pop"},
        {"Generale",                "Francesco De Gregori",  "Folk"},
        {"Caruso",                  "Lucio Dalla",           "Pop"},
        {"La locomotiva",           "Francesco Guccini",     "Folk"},
        {"E penso a te",            "Lucio Battisti",        "Pop"},
        {"Il mio canto libero",     "Lucio Battisti",        "Pop"},
        {"Senza una donna",         "Zucchero",              "Blues"},
        {"Ancora",                  "Eduardo De Crescenzo",  "Pop"},
        {"Il cielo in una stanza",  "Gino Paoli",            "Pop"}
    };

    private static final String[] COMMENTS = {
        "Bellissima melodia, uno dei classici italiani più amati",
        "Testo profondissimo, mi fa sempre venire i brividi",
        "Uno spartito meraviglioso da suonare al pianoforte",
        "La voce di questo artista è ineguagliabile",
        "Brano che racconta la storia italiana in modo unico",
        "Arrangiamento fantastico, ogni strumento si sente perfettamente",
        "Questo pezzo mi accompagna da quando ero piccolo",
        "La chitarra in questo brano è semplicemente perfetta",
        "Un capolavoro senza tempo, sempre attuale",
        "Ho imparato questo brano appena ho iniziato a studiare musica"
    };

    /**
     * Esegue il seeding del database e restituisce gli ID reali dei 10 utenti creati.
     * Sequenza:
     *   utente[0] carica songs[0], commenta songs[1]
     *   utente[1] carica songs[1], commenta songs[2]
     *   ...
     *   utente[9] carica songs[9], commenta songs[0]
     */
    public static int[] seed(UserDAO userDAO, SongDAO songDAO, CommentDAO commentDAO) {
        int n = USERS.length;
        int[] userIds = new int[n];
        int[] songIds = new int[n];

        // 1. Crea gli utenti e recupera i loro ID assegnati dal DB
        for (int i = 0; i < n; i++) {
            User user = new User(USERS[i][0], USERS[i][1], USERS[i][2], USERS[i][3], USERS[i][4]);
            userDAO.registerUser(user);
            userIds[i] = userDAO.login(USERS[i][2], USERS[i][3]).getId();
        }

        // 2. Ogni utente carica una canzone — uploader_id è l'ID reale dell'utente
        for (int i = 0; i < n; i++) {
            Song song = new Song(0, SONGS[i][0], SONGS[i][1], SONGS[i][2],
                    "", "", "", "",
                    userIds[i], USERS[i][0], USERS[i][1], "");
            songDAO.addSong(song);
        }

        // 3. Recupera gli ID reali delle canzoni filtrando per titolo E uploader_id,
        //    così il legame canzone → utente è esplicito e non ambiguo
        List<Song> allSongs = songDAO.getAllSongs();
        for (int i = 0; i < n; i++) {
            final String title      = SONGS[i][0];
            final int   uploaderId  = userIds[i];
            songIds[i] = allSongs.stream()
                    .filter(s -> title.equals(s.getTitle()) && s.getUploader_id() == uploaderId)
                    .mapToInt(Song::getId)
                    .findFirst()
                    .orElse(0);
        }

        // 4. Ogni utente commenta la canzone dell'utente successivo (circolare)
        //    Il comment.user_id e comment.song_id puntano entrambi a righe esistenti
        for (int i = 0; i < n; i++) {
            int targetSongId = songIds[(i + 1) % n];
            Comment comment = new Comment(0, targetSongId, 0, 0,
                    userIds[i], COMMENTS[i], 0, null, "Pending");
            commentDAO.addComment(comment, CommentManager.ResourceType.SONG, targetSongId);
        }

        return userIds;
    }
}
