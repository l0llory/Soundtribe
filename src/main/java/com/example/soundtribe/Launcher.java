package com.example.soundtribe;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class Launcher extends Application {

    private static Launcher instance;

    public static Launcher getInstance() {
        return instance;
    }


    @Override
    public void start(Stage stage) throws IOException {
        instance = this; // Salva l'istanza per permettere ai controller di chiamarla

        // Carica la scena iniziale (Autenticazione)
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/com/example/soundtribe/view/Autenticazione.fxml"));
        // Rimuoviamo le dimensioni fisse da qui, se ci sono problemi con il calcolo
        // dei bordi finestra nativi. Impostiamo dimensioni minime nello stage se necessario
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        String cssPath = getClass().getResource("/com/example/soundtribe/css/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        // Imposta l'icona dell'applicazione
        try {
            // Usa il percorso relativo dalla root del classpath (resources)
            Image icon = new Image(getClass().getResourceAsStream("/com/example/soundtribe/img/soundtribe-logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'icona dell'applicazione: " + e.getMessage());
        }

        stage.setTitle("SoundTribe");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public void openDocument(String url) {
        getHostServices().showDocument(url);
    }

    public static void main(String[] args) {
        launch();
    }
}
