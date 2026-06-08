package com.example.soundtribe.controller;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
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
        // Rimuoviamo gli spazi vuoti accidentali prima o dopo la stringa
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            AlertUtil.mostra("Errore di accesso", "Campi incompleti",
                    "Per favore, inserisci sia l'email che la password.", AlertType.WARNING);
            return;
        }

        System.out.println("Tentativo di login con Email: '" + email + "' e Password: '" + password + "'"); // LOG PER DEBUG

        UserDAO userDAO = new UserDAO();
        User loggedUser = userDAO.login(email, password);

        if (loggedUser != null) {

            String status = loggedUser.getStatus() != null ? loggedUser.getStatus() : "Pending";

            switch (status) {
                case "Verified":
                    UserSession.getInstance().setUserId(loggedUser.getId());
                    UserSession.getInstance().setIsAdmin(loggedUser.isAdmin());
                    SceneManager.changeScene(event, "Home.fxml", true);
                    break;

                case "Pending":
                    AlertUtil.mostra("Accesso Negato", "Account in attesa",
                            "Il tuo account è ancora in fase di approvazione. Attendi che l'amministratore lo verifichi.", AlertType.WARNING);
                    break;

                case "Banned":
                    String Motivation=loggedUser.getMotivation();
                    if (Motivation!= null && !Motivation.trim().isEmpty()) {

                        AlertUtil.mostra("Accesso Bloccato", "Sei stato bannato",
                                "Questo account è stato sospeso permanentemente: "+ Motivation, AlertType.ERROR);
                        break;
                    }else {
                        AlertUtil.mostra("Accesso Bloccato", "Sei stato bannato",
                                "Questo account è stato sospeso permanentemente per violazione dei termini del servizio.", AlertType.ERROR);
                    }

                default:
                    AlertUtil.mostra("Errore", "Stato non riconosciuto",
                            "Si è verificato un problema imprevisto con il tuo account.", AlertType.ERROR);
                    break;
            }

        } else {
            // Se loggedUser è null significa che Email o Password non esistono nel DB
            AlertUtil.mostra("Errore Login", "Credenziali errate", "Email o password non valide", AlertType.ERROR);
        }
    }

    private void handleGoToRegistration(ActionEvent event) {
        SceneManager.changeScene(event, "Registrazione.fxml", false);
    }
}
