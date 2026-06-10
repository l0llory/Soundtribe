package com.example.soundtribe.controller;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegistrationController {

    @FXML
    public Button backToAuthentication;
    @FXML
    public Button handleAccessByRegistration;
    @FXML
    public TextField nameField;
    @FXML
    public TextField surnameField;
    @FXML
    public TextField emailField2;
    @FXML
    public PasswordField passwordField2;

    // NUOVO CAMPO FXML
    @FXML
    public TextArea motivationField;

    public void initialize() {
        if (handleAccessByRegistration != null) {
            handleAccessByRegistration.setOnAction(this::handleRegistration);
        }

        if (backToAuthentication != null) {
            backToAuthentication.setOnAction(this::handleBackToAuthentication);
        }
    }

    private void handleBackToAuthentication(ActionEvent event) {
        SceneManager.changeScene(event, "Autenticazione.fxml", false);
    }

    private void handleRegistration(ActionEvent event) {

        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String email = emailField2.getText().trim();
        String password = passwordField2.getText();
        String motivation = motivationField.getText().trim();

        // 1. Controllo campi vuoti (inclusa motivazione)
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || motivation.isEmpty()) {
            AlertUtil.mostra("Errore di registrazione", "Campi incompleti",
                    "Per favore, compila tutti i campi, inclusa la motivazione per l'iscrizione.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Creazione oggetto User
        // NOTA: Assicurati di aver aggiornato la classe User per avere il metodo setMotivation() o un costruttore adeguato.
        User newUser = new User(name, surname, email, password, "Nessuno");
        newUser.setMotivation(motivation); // Impostiamo la motivazione

        UserDAO userDAO = new UserDAO();

        // 3. Tentativo di registrazione nel Database
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            // REGISTRAZIONE RIUSCITA - IN ATTESA DI APPROVAZIONE
            AlertUtil.mostra("Richiesta Inviata",
                    "In attesa di valutazione",
                    "La tua richiesta di iscrizione e le tue motivazioni sono state inviate all'amministratore.\nRiceverai una notifica (email) non appena il tuo account sarà attivato.",
                    Alert.AlertType.INFORMATION);

            // Rimandiamo l'utente alla schermata di Login
            SceneManager.changeScene(event, "Autenticazione.fxml", false);

        } else {
            // REGISTRAZIONE FALLITA
            AlertUtil.mostra("Errore di registrazione", "Impossibile registrare l'utente",
                    "L'email inserita potrebbe essere già in uso o c'è un problema col server.", Alert.AlertType.ERROR);
        }
    }
}