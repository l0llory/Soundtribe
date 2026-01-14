package com.example.soundtribe;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

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
        stage.setTitle("SoundTribe");
        stage.setScene(scene);
        stage.show();
    }

    // Metodo magico per Linux: apre URL e File senza bloccare l'app
    public void openDocument(String uri) {
        getHostServices().showDocument(uri);
    }

    public static void main(String[] args) {
        launch();
    }
}