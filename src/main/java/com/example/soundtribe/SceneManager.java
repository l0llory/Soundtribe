package com.example.soundtribe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class SceneManager {
    public static void changeScene(ActionEvent event, String fxml, boolean addToHistory){
        try {
            // 1. Se è una nuova pagina, la salviamo nella cronologia
            if (addToHistory) {
                NavigationManager.navigateTo(fxml);
            }

            // Ottieni la finestra corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();

            // 2. Carichiamo il file FXML
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), width, height);

            scene.getStylesheets().add(SceneManager.class.getResource("/com/example/soundtribe/css/style.css").toExternalForm());

            // 3. Prendiamo la finestra attuale e cambiamo il contenuto
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore: impossibile caricare la seguente pagina: " + fxml);
            e.printStackTrace();
        }

    }
}
