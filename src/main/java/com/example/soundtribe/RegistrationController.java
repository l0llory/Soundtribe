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
            backToAuthentication.setOnAction(actionEvent -> handleBackToRegistration(actionEvent));
        }
    }

    //Gestisce il ritorno alla schermata di autenticazione
    private void handleBackToRegistration(ActionEvent actionEvent) {
        try {
            System.out.println("Navigazione verso la schermata di autenticazione...");
            FXMLLoader fxmlLoaderBackAuth = new FXMLLoader(RegistrationController.class.getResource("Autenticazione.fxml"));
            Scene scene = new Scene(fxmlLoaderBackAuth.load(), 600, 600);

            // Recupera la finestra (Stage) corrente
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            stage.setTitle("SoundTribe - Home");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.mostra("Errore", "Errore di caricamento", "Impossibile caricare la schermata di autenticazione.", Alert.AlertType.ERROR);
        }


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
        try {
            System.out.println("Navigazione verso la home...");
            FXMLLoader fxmlLoaderHome = new FXMLLoader(RegistrationController.class.getResource("Home.fxml"));
            Scene scene = new Scene(fxmlLoaderHome.load(), 800, 600);

            // Recupera la finestra (Stage) corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("SoundTribe - Home");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.mostra("Errore", "Errore di caricamento", "Impossibile caricare la schermata home.", Alert.AlertType.ERROR);
        }

    }



}
