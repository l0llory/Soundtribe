package com.example.soundtribe.manager;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.item.UserSession;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class NavigationManager {

    private static final int MAX_HISTORY_SIZE = 20;

    private static final List<String> history = new ArrayList<>();
    private static int currentIndex = -1;

    // Chiamato da SceneManager ogni volta che si naviga verso una nuova pagina
    public static void navigateTo(String fxml) {
        if (currentIndex >= 0 && history.get(currentIndex).equals(fxml)) {
            return;
        }
        // Se eravamo andati indietro e ora navighiamo in avanti, la storia "futura" viene cancellata
        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }
        history.add(fxml);
        currentIndex++;

        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
            currentIndex--;
        }
    }

    /**
     * Configura il bottone ◀: imposta lo stato disabilitato e il gestore click.
     * Può essere chiamato da solo oppure tramite updateNavigationButtons.
     */
    public static void navBack(Button button) {
        if (button == null) return;
        button.setDisable(currentIndex <= 0);
        button.setOnAction(event -> {
            if (currentIndex > 0) {
                currentIndex--;
                SceneManager.changeScene(event, history.get(currentIndex), false);
            }
        });
    }

    /**
     * Configura il bottone ▶: imposta lo stato disabilitato e il gestore click.
     * Può essere chiamato da solo oppure tramite updateNavigationButtons.
     */
    public static void navForward(Button button) {
        if (button == null) return;
        button.setDisable(currentIndex >= history.size() - 1);
        button.setOnAction(event -> {
            if (currentIndex < history.size() - 1) {
                currentIndex++;
                SceneManager.changeScene(event, history.get(currentIndex), false);
            }
        });
    }

    /**
     * Metodo principale da usare nei controller: configura entrambi i bottoni
     * di navigazione in un'unica chiamata. Il parametro forwardBtn può essere null
     * per le pagine che non hanno il bottone avanti.
     */
    public static void updateNavigationButtons(Button backBtn, Button forwardBtn) {
        navBack(backBtn);
        navForward(forwardBtn);
    }

    public static void exit(Button button) {
        button.setOnAction(event -> {
            AlertUtil.showLogoutConfirmation(() -> {
                UserSession.getInstance().cleanUserSession();
                history.clear();
                currentIndex = -1;
                SceneManager.changeScene(event, "Autenticazione.fxml", false);
            });
        });
    }

    public static void home(Button button) {
        if (button == null) return;
        button.setOnAction(event -> SceneManager.changeScene(event, "Home.fxml", true));
    }
}
