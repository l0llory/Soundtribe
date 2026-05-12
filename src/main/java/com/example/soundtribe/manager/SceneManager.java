package com.example.soundtribe.manager;

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

            // Ottieni la SCENA corrente (e non lo Stage) per calcolare la larghezza e l'altezza
            // del contenuto senza i bordi nativi di Windows
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            double width = currentScene.getWidth();
            double height = currentScene.getHeight();

            // Salviamo lo stato "massimizzato" PRIMA di cambiare la scena
            boolean isMaximized = stage.isMaximized();

            // 2. Carichiamo il file FXML
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/example/soundtribe/view/" + fxml));
            
            // Creiamo la scena passando la larghezza e l'altezza della scena precedente
            Scene newScene = new Scene(loader.load(), width, height);
            newScene.getStylesheets().add(SceneManager.class.getResource("/com/example/soundtribe/css/style.css").toExternalForm());

            // 3. Applichiamo la scena
            stage.setScene(newScene);
            
            // Riapplichiamo lo stato massimizzato, se necessario
            if (isMaximized) {
                stage.setMaximized(true);
            }
            
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore: impossibile caricare la seguente pagina: " + fxml);
            e.printStackTrace();
        }

    }
}
