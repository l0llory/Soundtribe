package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
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

    public void initialize() {
        if (handleAccessByRegistration != null) {
            handleAccessByRegistration.setOnAction(this::handleRegistration);
        }

        if (backToAuthentication != null) {
            backToAuthentication.setOnAction(this::handleBackToAuthentication);
        }
    }

    private void handleBackToAuthentication(ActionEvent event) {
        SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, false);
    }

    private void handleRegistration(ActionEvent event) {

        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String email = emailField2.getText().trim();
        String password = passwordField2.getText();

        // 1. Controllo campi vuoti
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            AlertUtil.mostra("Errore di registrazione", "Campi incompleti",
                    "Per favore, compila tutti i campi.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Creazione oggetto User
        // Usiamo il costruttore: name, surname, email, password, isApproved (FALSE)
        // Passiamo 'false' perché l'utente deve essere approvato dall'admin
        User newUser = new User(name, surname, email, password, "Nessuno");
        UserDAO userDAO = new UserDAO();

        // 3. Tentativo di registrazione nel Database
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            // REGISTRAZIONE RIUSCITA

            // MODIFICA IMPORTANTE: Non facciamo più il login automatico.
            // Avvisiamo l'utente che la richiesta è in attesa di approvazione.

            AlertUtil.mostra("Registrazione Inviata",
                    "In attesa di approvazione",
                    "Il tuo account è stato creato correttamente. \nUn amministratore dovrà approvare la tua richiesta prima che tu possa effettuare il login.",
                    Alert.AlertType.INFORMATION);

            // Rimandiamo l'utente alla schermata di Login
            SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, false);

        } else {
            // REGISTRAZIONE FALLITA (Probabilmente email duplicata)
            AlertUtil.mostra("Errore di registrazione", "Impossibile registrare l'utente",
                    "L'email inserita potrebbe essere già in uso o c'è un problema col server.", Alert.AlertType.ERROR);
        }
    }
}