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
    private Button handleAccessByRegistration;
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField emailField2;
    @FXML
    private PasswordField passwordField2;

    // Gestisce l'inizializzazione dei bottoni
    public void initialize() {
        if (handleAccessByRegistration != null) {
            handleAccessByRegistration.setOnAction(this::handleRegistration);
        }

        if (backToAuthentication != null) {
            backToAuthentication.setOnAction(this::handleBackToAuthentication);
        }
    }

    // Gestisce il ritorno alla schermata di autenticazione (Login)
    private void handleBackToAuthentication(ActionEvent event) {
        SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, false);
    }

    // Gestisce la logica di registrazione
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

        // 2. Creazione oggetto User e DAO
        // Usiamo il costruttore senza ID (lo genererà il database)
        User newUser = new User(name, surname, email, password);
        UserDAO userDAO = new UserDAO();

        // 3. Tentativo di registrazione nel Database
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            // REGISTRAZIONE RIUSCITA

            // Per entrare subito nella Home, dobbiamo recuperare l'ID appena creato
            // Facciamo un login "silenzioso" automatico
            User loggedUser = userDAO.login(email, password);

            if (loggedUser != null) {
                // Impostiamo la sessione globale
                UserSession.getInstance().setUserId(loggedUser.getId());
                UserSession.getInstance().setIsAdmin(loggedUser.isAdmin()); // Di default false, ma lo settiamo

                AlertUtil.mostra("Successo", "Registrazione completata!",
                        "Benvenuto in SoundTribe, " + name, Alert.AlertType.INFORMATION);

                // Vai alla Home
                SceneManager.changeScene(event, "Home.fxml", 800, 600, true);
            } else {
                // Caso raro: registrato ma login fallito subito dopo
                AlertUtil.mostra("Attenzione", "Registrazione avvenuta",
                        "Per favore effettua il login manualmente.", Alert.AlertType.INFORMATION);
                SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, false);
            }

        } else {
            // REGISTRAZIONE FALLITA (Probabilmente email duplicata)
            // Nota: Il tuo UserDAO cattura l'eccezione SQLState 23505 e ritorna false
            AlertUtil.mostra("Errore di registrazione", "Impossibile registrare l'utente",
                    "L'email inserita potrebbe essere già in uso.", Alert.AlertType.ERROR);
        }
    }
}