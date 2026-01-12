package com.example.soundtribe;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLXML;

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

    //Gestisce il bottone di tentativo di accesso
    public void initialize(){
        if(handleAccessByRegistration != null) {
            handleAccessByRegistration.setOnAction( event -> handleRegistration(event));
        }

        if(backToAuthentication != null){
            backToAuthentication.setOnAction(event -> handleBackToRegistration(event));
        }
    }

    //Gestisce il ritorno alla schermata di autenticazione
    private void handleBackToRegistration(ActionEvent event) {
       SceneManager.changeScene(event, "Autenticazione.fxml", 600, 600, false);
    }

    //Gestisce i campi di testo: se non vengono riempiti lancia un avviso
    private void handleRegistration(ActionEvent event){

        String name = nameField.getText();
        String surname = surnameField.getText();
        String email = emailField2.getText();
        String password = passwordField2.getText();

        if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            AlertUtil.mostra("Errore di accesso", "Campi incompleti", "Per favore," +
                    " inserisci sia l'email che la password.", Alert.AlertType.WARNING);
            return;
        }
        SceneManager.changeScene(event, "Home.fxml", 800, 600, false);

    }



}
