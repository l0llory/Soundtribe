package com.example.soundtribe.item;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Optional;

public class AlertUtil {

    private static final String CSS_PATH = "/com/example/soundtribe/css/style.css";

    /** Applica il tema scuro a qualsiasi DialogPane. */
    private static void applica(DialogPane dp) {
        URL css = AlertUtil.class.getResource(CSS_PATH);
        if (css != null) dp.getStylesheets().add(css.toExternalForm());
        dp.getStyleClass().add("st-alert");
        // Rende la scena del dialog scura appena viene creata
        dp.sceneProperty().addListener((obs, o, scene) -> {
            if (scene != null) scene.setFill(Color.web("#121212"));
        });
    }

    /** Mostra un messaggio (info / avviso / errore) e attende la chiusura. */
    public static void mostra(String titolo, String intestazione, String messaggio, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(intestazione);
        alert.setContentText(messaggio);
        applica(alert.getDialogPane());
        alert.showAndWait();
    }

    /**
     * Mostra un dialogo di conferma (OK / Annulla).
     * @return true se l'utente ha premuto OK.
     */
    public static boolean chiediConferma(String titolo, String intestazione, String messaggio) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(intestazione);
        alert.setContentText(messaggio);
        applica(alert.getDialogPane());
        return alert.showAndWait()
                    .filter(b -> b == ButtonType.OK)
                    .isPresent();
    }

    /**
     * Mostra un dialogo di input testo con tema scuro.
     * @return il testo inserito, o null se annullato o vuoto.
     */
    public static String chiediTesto(String titolo, String intestazione, String etichetta) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(titolo);
        dialog.setHeaderText(intestazione);
        dialog.setContentText(etichetta);
        applica(dialog.getDialogPane());
        return dialog.showAndWait()
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .orElse(null);
    }

    /** Dialogo di conferma logout. */
    public static void showLogoutConfirmation(Runnable onConfirm) {
        if (chiediConferma("Conferma Logout",
                "Sei sicuro di voler uscire?",
                "Verrai reindirizzato alla schermata di login.")) {
            onConfirm.run();
        }
    }
}
