package com.example.soundtribe;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class AuthenticationController extends Application {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button goToRegistrationButton;

    @Override

    //Carica la scena di autenticazione
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AuthenticationController.class.getResource("Autenticazione.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        stage.setTitle("SoundTribe - Accedi");
        stage.setScene(scene);
        stage.show();
    }
    // Gestisce l'uso dei bottoni di login e di registrazione...
    public void initialize() {
        if (loginButton != null) {
            loginButton.setOnAction(event -> handleLogin(event));
        }
        if (goToRegistrationButton != null) {
            goToRegistrationButton.setOnAction(event -> handleGoToRegistration(event));
        }
    }
    // Gestisce i campi emailField e passwordField e la logica di accesso al database...
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            AlertUtil.mostra("Errore di accesso", "Campi incompleti",
                    "Per favore, inserisci sia l'email che la password.", AlertType.WARNING);
            return;
        }

        SceneManager.changeScene(event, "Home.fxml", 800, 600, false);

        System.out.println("Tentativo di accesso per: " + email);
        // Logica di autenticazione qui e gestione Database...
    }
    // Gestisce il reindirizzamento verso la schermata di registrazione
    private void handleGoToRegistration(ActionEvent event) {
        SceneManager.changeScene(event, "Registrazione.fxml", 600, 600, false);
    }

}