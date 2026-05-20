package com.example.soundtribe.controller;

import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import java.net.URL;

public class UserProfileViewController {

    @FXML public Button backButton;
    @FXML public Circle profileCircle;
    @FXML public Label fullNameLabel;
    @FXML public Label roleLabel;
    @FXML public Label emailLabel;
    @FXML public Label statusLabel;
    @FXML public Label genreLabel;
    @FXML public Label idLabel;

    public void initialize() {
        backButton.setOnAction(event -> SceneManager.changeScene(event, "gestioneUtenti.fxml", true));
    }

    // METODO RICEVENTE: Chiamato dinamicamente da UsersController
    public void setTargetUser(User user) {
        if (user == null) return;

        // 1. Assegnazione Dati Testuali
        fullNameLabel.setText(user.getName() + " " + user.getSurname());
        emailLabel.setText(user.getEmail());
        idLabel.setText("#" + user.getId());

        if (user.getFavoriteGenre() != null && !user.getFavoriteGenre().isEmpty()) {
            genreLabel.setText(user.getFavoriteGenre());
        } else {
            genreLabel.setText("Non specificato");
        }

        // 2. Rendering grafico dei Ruoli con stile Badge
        if (user.isAdmin()) {
            roleLabel.setText("ADMIN");
            roleLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else {
            roleLabel.setText("USER");
            roleLabel.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        }

        // 3. Rendering visivo dello Stato Account
        if (user.isApproved()) {
            statusLabel.setText("✔ Attivo");
            statusLabel.setTextFill(Color.web("#27ae60"));
        } else {
            statusLabel.setText("⏳ In Attesa");
            statusLabel.setTextFill(Color.web("#f39c12"));
        }

        // 4. Caricamento Immagine Profilo Dedicata
        loadProfileImage(user);
    }

    private void loadProfileImage(User user) {
        try {
            URL defaultUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
            Image defaultImg = (defaultUrl != null) ? new Image(defaultUrl.toExternalForm()) : null;

            if (user.getProfilePicPath() != null && !user.getProfilePicPath().isEmpty()) {
                Image userImg = new Image(user.getProfilePicPath());
                if (userImg.isError()) {
                    if (defaultImg != null) profileCircle.setFill(new ImagePattern(defaultImg));
                    else profileCircle.setFill(Color.LIGHTGRAY);
                } else {
                    profileCircle.setFill(new ImagePattern(userImg));
                }
            } else {
                if (defaultImg != null) profileCircle.setFill(new ImagePattern(defaultImg));
                else profileCircle.setFill(Color.LIGHTGRAY);
            }
        } catch (Exception e) {
            profileCircle.setFill(Color.LIGHTGRAY);
        }
    }
}