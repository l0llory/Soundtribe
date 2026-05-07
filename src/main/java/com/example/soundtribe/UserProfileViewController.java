package com.example.soundtribe;

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
        // Il bottone indietro torna alla gestione utenti
        backButton.setOnAction(event -> SceneManager.changeScene(event, "gestioneUtenti.fxml", true));
    }

    // METODO FONDAMENTALE: Riceve l'utente da visualizzare
    public void setTargetUser(User user) {
        if (user == null) return;

        // 1. Dati Testuali
        fullNameLabel.setText(user.getName() + " " + user.getSurname());
        emailLabel.setText(user.getEmail());
        idLabel.setText("#" + user.getId());

        // Genere (Gestione null)
        if (user.getFavoriteGenre() != null && !user.getFavoriteGenre().isEmpty()) {
            genreLabel.setText(user.getFavoriteGenre());
        } else {
            genreLabel.setText("Non specificato");
        }

        // 2. Ruolo (Stile Badge)
        if (user.isAdmin()) {
            roleLabel.setText("ADMIN");
            roleLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        } else {
            roleLabel.setText("USER");
            roleLabel.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        }

        // 3. Stato (Approvato/In Attesa)
        if (user.isApproved()) {
            statusLabel.setText("✔ Attivo");
            statusLabel.setTextFill(Color.web("#27ae60")); // Verde
        } else {
            statusLabel.setText("⏳ In Attesa");
            statusLabel.setTextFill(Color.web("#f39c12")); // Arancione
        }

        // 4. Immagine Profilo
        loadProfileImage(user);
    }

    private void loadProfileImage(User user) {
        try {
            // Immagine di default
            URL defaultUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
            Image defaultImg = (defaultUrl != null) ? new Image(defaultUrl.toExternalForm()) : null;

            if (user.getProfilePicPath() != null && !user.getProfilePicPath().isEmpty()) {
                Image userImg = new Image(user.getProfilePicPath());
                if (userImg.isError()) {
                    // Se il percorso è sbagliato, usa default
                    if (defaultImg != null) profileCircle.setFill(new ImagePattern(defaultImg));
                    else profileCircle.setFill(Color.LIGHTGRAY);
                } else {
                    profileCircle.setFill(new ImagePattern(userImg));
                }
            } else {
                // Se non ha foto, usa default
                if (defaultImg != null) profileCircle.setFill(new ImagePattern(defaultImg));
                else profileCircle.setFill(Color.LIGHTGRAY);
            }
        } catch (Exception e) {
            profileCircle.setFill(Color.LIGHTGRAY);
        }
    }
}