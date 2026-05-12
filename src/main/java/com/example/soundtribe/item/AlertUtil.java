package com.example.soundtribe.item;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertUtil {
    public static void mostra(String titolo, String intestazione, String messaggio, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(intestazione);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
