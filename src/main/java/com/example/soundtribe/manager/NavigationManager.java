package com.example.soundtribe.manager;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.item.UserSession;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class NavigationManager {
    private static final int MAX_HISTORY_SIZE = 3;
    
    // Lista che rappresenta la cronologia delle pagine visitate
    private static final List<String> history = new ArrayList<>();
    
    // Indice della pagina corrente nella cronologia
    private static int currentIndex = -1;

    // --- METODO CHIAMATO QUANDO SI APRE UNA NUOVA PAGINA ---
    public static void navigateTo(String fxml) {
        // Evita di aggiungere la pagina corrente alla cronologia se è già l'ultima
        if (currentIndex >= 0 && history.get(currentIndex).equals(fxml)) {
            return;
        }

        // Se eravamo andati "indietro" e poi navighiamo verso una nuova pagina,
        // la cronologia "in avanti" (le pagine dopo currentIndex) viene cancellata.
        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }

        // Aggiungi la nuova pagina
        history.add(fxml);
        currentIndex++;

        // Mantieni la dimensione massima della cronologia a MAX_HISTORY_SIZE
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0); // Rimuovi il First In
            currentIndex--;    // L'indice scala all'indietro perché gli elementi sono slittati
        }
    }

    // --- LOGICA PULSANTE INDIETRO ---
    public static void navBack(Button button) {
        button.setOnAction(event -> {
            if (currentIndex > 0) {
                currentIndex--;
                String previousPage = history.get(currentIndex);
                System.out.println("Going Back to: " + previousPage);
                // addToHistory = false perché stiamo solo navigando nella cronologia esistente
                SceneManager.changeScene(event, previousPage, false);
            }
        });
    }

    // --- LOGICA PULSANTE AVANTI ---
    public static void navForward(Button button) {
        button.setOnAction(event -> {
            if (currentIndex < history.size() - 1) {
                currentIndex++;
                String nextPage = history.get(currentIndex);
                System.out.println("Going Forward to: " + nextPage);
                // addToHistory = false perché stiamo solo navigando nella cronologia esistente
                SceneManager.changeScene(event, nextPage, false);
            }
        });
    }

    // --- AGGIORNAMENTO STATO BOTTONI ---
    public static void updateNavigationButtons(Button backBtn, Button forwardBtn) {
        // Abilita il pulsante indietro solo se non siamo al primo elemento della cronologia
        if (backBtn != null) {
            backBtn.setDisable(currentIndex <= 0);
            navBack(backBtn);
        }

        // Abilita il pulsante avanti solo se non siamo all'ultimo elemento della cronologia
        if (forwardBtn != null) {
            forwardBtn.setDisable(currentIndex >= history.size() - 1);
            navForward(forwardBtn);
        }
    }

    // --- GESTIONE USCITA E RESET ---
    public static void exit(Button button) {
        button.setOnAction(event -> {
            AlertUtil.showLogoutConfirmation(() -> {
                UserSession.getInstance().cleanUserSession();
                
                // Resetta la cronologia al logout
                history.clear();
                currentIndex = -1;

                SceneManager.changeScene(event, "Autenticazione.fxml", false);
            });
        });
    }

    public static void home(Button button) {
        button.setOnAction(event -> {
            // Andare alla Home è una "Nuova Navigazione", quindi addToHistory = true
            SceneManager.changeScene(event, "Home.fxml", true);
        });
    }
}
