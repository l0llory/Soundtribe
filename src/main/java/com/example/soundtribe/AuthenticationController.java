package com.example.soundtribe;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
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
    // Mostra un allert se avviene un errore...
    public void mostraAlert(String titolo, String intestazione, String messaggio, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(intestazione);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    // Gestisce l'uso dei bottoni di login e di registrazione...
    public void initialize() {
        if (loginButton != null) {
            loginButton.setOnAction(event -> handleLogin());
        }
        if (goToRegistrationButton != null) {
            goToRegistrationButton.setOnAction(event -> handleGoToRegistration(event));
        }
    }
    // Gestisce i campi emailField e passwordField e la logica di accesso al database...
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostraAlert("Errore di accesso", "Campi incompleti", "Per favore, inserisci sia l'email che la password.", AlertType.WARNING);
            return;
        }

        System.out.println("Tentativo di accesso per: " + email);
        // Logica di autenticazione qui e gestione Database...
    }
    // Gestisce il reindirizzamento verso la schermata di registrazione
    private void handleGoToRegistration(ActionEvent event) {
        try {
            System.out.println("Navigazione verso la registrazione...");
            FXMLLoader fxmlLoaderReg = new FXMLLoader(AuthenticationController.class.getResource("Registrazione.fxml"));
            Scene scene = new Scene(fxmlLoaderReg.load(), 600, 600);

            // Recupera la finestra (Stage) corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("SoundTribe - Registrati");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostraAlert("Errore", "Errore di caricamento", "Impossibile caricare la schermata di registrazione.", AlertType.ERROR);
        }
    }

}