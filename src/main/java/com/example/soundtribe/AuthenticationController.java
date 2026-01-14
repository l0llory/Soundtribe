package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;

public class AuthenticationController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button goToRegistrationButton;

    @FXML
    public void initialize() {
        if (loginButton != null) {
            loginButton.setOnAction(this::handleLogin);
        }
        if (goToRegistrationButton != null) {
            goToRegistrationButton.setOnAction(this::handleGoToRegistration);
        }
    }

    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            AlertUtil.mostra("Errore di accesso", "Campi incompleti",
                    "Per favore, inserisci sia l'email che la password.", AlertType.WARNING);
            return;
        }

        System.out.println("Tentativo di accesso per: " + email);
        SceneManager.changeScene(event, "Home.fxml", 800, 600, true);
    }

    private void handleGoToRegistration(ActionEvent event) {
        SceneManager.changeScene(event, "Registrazione.fxml", 600, 600, false);
    }
}