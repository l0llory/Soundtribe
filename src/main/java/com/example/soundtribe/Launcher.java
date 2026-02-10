package com.example.soundtribe;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("Autenticazione.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        String cssPath = getClass().getResource("/com/example/soundtribe/css/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        URL cssUrl = getClass().getResource("css/style.css");
        if (cssUrl == null) {
            System.out.println("ERRORE CRITICO: File CSS non trovato!");
        } else {
            System.out.println("CSS Trovato: " + cssUrl.toExternalForm());
        }


        stage.setTitle("SoundTribe");
        stage.setScene(scene);
        stage.show();
    }

    public void openDocument(String uri) {
        getHostServices().showDocument(uri);
    }

    public static void main(String[] args) {
        launch();
    }
}
