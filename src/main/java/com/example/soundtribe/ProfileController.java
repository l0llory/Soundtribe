package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern; // IMPORT NECESSARIO
import javafx.scene.shape.Circle;       // IMPORT NECESSARIO
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;

public class ProfileController {

    @FXML public Button backBtn;
    @FXML public Button homeBtn;
    @FXML public Button logoutBtn;

    // --- MODIFICA 1: Usa Circle invece di ImageView ---
    @FXML public Circle profileCircle;
    @FXML public Button changePicBtn;

    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public ComboBox<String> genreCombo;

    @FXML public Button saveBtn;
    @FXML public Button cancelBtn;

    private User currentUser;
    private UserDAO userDAO;
    private String tempProfilePicPath = null;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        NavigationManager.navBack(backBtn);
        NavigationManager.home(homeBtn);
        NavigationManager.exit(logoutBtn);

        genreCombo.getItems().addAll("Nessuno", "Rock", "Pop", "Jazz", "Classica", "Hip Hop", "Indie");
        loadUserData();

        // --- MODIFICA 3: Aggiorna il caricamento da file ---
        changePicBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                tempProfilePicPath = file.toURI().toString();
                // Usa ImagePattern per riempire il cerchio
                profileCircle.setFill(new ImagePattern(new Image(tempProfilePicPath)));
            }
        });

        saveBtn.setOnAction(e -> saveChanges(e));
    }

    private void loadUserData() {
        int userId = UserSession.getInstance().getUserId();
        currentUser = userDAO.getUserById(userId);

        if (currentUser != null) {
            usernameField.setText(currentUser.getName());
            genreCombo.setValue(currentUser.getFavoriteGenre() != null ? currentUser.getFavoriteGenre() : "Nessuno");

            // --- MODIFICA 2: Aggiorna il caricamento dati utente ---
            if (currentUser.getProfilePicPath() != null && !currentUser.getProfilePicPath().isEmpty()) {
                try {
                    // Usa ImagePattern per riempire il cerchio
                    profileCircle.setFill(new ImagePattern(new Image(currentUser.getProfilePicPath())));
                    tempProfilePicPath = currentUser.getProfilePicPath();
                } catch (Exception e) {
                    setAnonymousImage();
                }
            } else {
                setAnonymousImage();
            }
            // Non serve più chiamare centerImage()
        }
    }

    // --- MODIFICA 4: Aggiorna l'immagine di default ---
    private void setAnonymousImage() {
        try {
            URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
            if (imageUrl != null) {
                // Usa ImagePattern per riempire il cerchio
                profileCircle.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: cerchio grigio se non trova l'immagine
            profileCircle.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        }
    }

    // --- MODIFICA 5: Rimuovi il metodo centerImage() ---
    // private void centerImage() { ... }  <- RIMOSSO

    private void saveChanges(ActionEvent event) {
        // ... (questo metodo rimane invariato) ...
        if (currentUser == null) return;

        currentUser.setName(usernameField.getText());
        currentUser.setFavoriteGenre(genreCombo.getValue());

        if (tempProfilePicPath != null) {
            currentUser.setProfilePicPath(tempProfilePicPath);
        }

        String newPass = passwordField.getText();
        if (!newPass.isEmpty()) {
            currentUser.setPassword(newPass);
        }

        boolean success = userDAO.updateUser(currentUser);

        if (success) {
            AlertUtil.mostra("Successo", "Profilo Aggiornato", "I tuoi dati sono stati salvati.", Alert.AlertType.INFORMATION);
            SceneManager.changeScene(event, "Home.fxml", 800, 600, true);
        } else {
            AlertUtil.mostra("Errore", "Errore salvataggio", "Impossibile aggiornare i dati nel database.", Alert.AlertType.ERROR);
        }
    }
}