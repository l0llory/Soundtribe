package com.example.soundtribe;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.util.Stack;

public class NavigationManager {
    // Usiamo Stack per simulare la cronologia del browser
    private static final Stack<String> backStack = new Stack<>();
    private static final Stack<String> forwardStack = new Stack<>();

    // --- METODO CHIAMATO QUANDO SI APRE UNA NUOVA PAGINA (NON BACK/FORWARD) ---
    public static void navigateTo(String fxml) {
        if (!backStack.isEmpty() && backStack.peek().equals(fxml)) {
            return;
        }
        backStack.push(fxml);
        forwardStack.clear();
    }

    // --- LOGICA PULSANTE INDIETRO ---
    public static void navBack(Button button) {
        // Aggiorniamo stato iniziale
        button.setDisable(backStack.size() <= 1);

        button.setOnAction(event -> {
            if (backStack.size() > 1) {

                String currentPage = backStack.pop();
                forwardStack.push(currentPage);


                String previousPage = backStack.peek();


                System.out.println("Going Back to: " + previousPage);
                SceneManager.changeScene(event, previousPage, false);

                button.setDisable(backStack.size() <= 1);
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
                SceneManager.changeScene(event, nextPage, false);
                
                // Aggiorniamo lo stato
                button.setDisable(forwardStack.isEmpty());
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
        
        // Colleghiamo anche le azioni
        navBack(backBtn);
        navForward(forwardBtn);
    }

    // --- GESTIONE USCITA E RESET ---
    public static void exit(Button button) {
        button.setOnAction(event -> {
            UserSession.getInstance().cleanUserSession();
            backStack.clear();
            forwardStack.clear();
            // Quando esco e vado al login, NON voglio che il login finisca nella cronologia
            SceneManager.changeScene(event, "Autenticazione.fxml", false);
        });
    }

    public static void home(Button button) {
        button.setOnAction(event -> {
            // Andare alla Home è una "Nuova Navigazione", quindi addToHistory = true
            SceneManager.changeScene(event, "Home.fxml", true);
        });
    }
}
