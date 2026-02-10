package com.example.soundtribe;

import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.util.Stack;

public class NavigationManager {
    // Usiamo Stack per simulare la cronologia del browser
    private static final Stack<String> backStack = new Stack<>();
    private static final Stack<String> forwardStack = new Stack<>();

    // --- METODO CHIAMATO QUANDO SI APRE UNA NUOVA PAGINA (NON BACK/FORWARD) ---
    public static void navigateTo(String fxml) {
        // Evitiamo di aggiungere duplicati consecutivi (es. clicco Home mentre sono in Home)
        if (!backStack.isEmpty() && backStack.peek().equals(fxml)) {
            return;
        }

        // Aggiungo alla storia "Indietro"
        backStack.push(fxml);
        // Se navigo in una pagina nuova, la storia "Avanti" deve essere distrutta (come Chrome)
        forwardStack.clear();

        // Debug
        System.out.println("History Push: " + fxml + " | BackStack Size: " + backStack.size());
    }

    // --- LOGICA PULSANTE INDIETRO ---
    public static void navBack(Button button) {
        // Aggiorniamo stato iniziale
        button.setDisable(backStack.size() <= 1);

        button.setOnAction(event -> {
            if (backStack.size() > 1) {
                // 1. Tolgo la pagina corrente dalla backStack e la metto nella forwardStack
                String currentPage = backStack.pop();
                forwardStack.push(currentPage);

                // 2. Guardo quale è la pagina precedente
                String previousPage = backStack.peek();

                // 3. Cambio scena SENZA AGGIUNGERE ALLA STORIA (addToHistory = false)
                System.out.println("Going Back to: " + previousPage);
                SceneManager.changeScene(event, previousPage, 800, 600, false);
            }
        });
    }

    // --- LOGICA PULSANTE AVANTI ---
    public static void navForward(Button button) {
        // Aggiorniamo stato iniziale
        button.setDisable(forwardStack.isEmpty());

        button.setOnAction(event -> {
            if (!forwardStack.isEmpty()) {
                // 1. Prendo la pagina futura
                String nextPage = forwardStack.pop();

                // 2. La rimetto nella backStack (perché ora diventa la pagina corrente)
                backStack.push(nextPage);

                // 3. Cambio scena SENZA AGGIUNGERE ALLA STORIA (addToHistory = false)
                System.out.println("Going Forward to: " + nextPage);
                SceneManager.changeScene(event, nextPage, 800, 600, false);
            }
        });
    }

    // --- AGGIORNAMENTO STATO BOTTONI ---
    // Da chiamare nel metodo initialize() di ogni controller
    public static void updateNavigationButtons(Button backBtn, Button forwardBtn) {
        // Posso andare indietro se c'è più di 1 elemento (l'elemento 1 è la pagina corrente)
        backBtn.setDisable(backStack.size() <= 1);

        // Posso andare avanti se c'è qualcosa nello stack forward
        forwardBtn.setDisable(forwardStack.isEmpty());
    }

    // --- GESTIONE USCITA E RESET ---
    public static void exit(Button button) {
        button.setOnAction(event -> {
            UserSession.getInstance().cleanUserSession();
            backStack.clear();
            forwardStack.clear();
            // Quando esco e vado al login, NON voglio che il login finisca nella cronologia
            SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, false);
        });
    }

    public static void home(Button button) {
        button.setOnAction(event -> {
            // Andare alla Home è una "Nuova Navigazione", quindi addToHistory = true
            SceneManager.changeScene(event, "Home.fxml", 800, 600, true);
        });
    }
}