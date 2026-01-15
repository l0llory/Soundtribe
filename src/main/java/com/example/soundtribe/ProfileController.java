package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;

public class ProfileController {

    @FXML public Button backBtn;
    @FXML public Button homeBtn;
    @FXML public Button logoutBtn;

    @FXML public ImageView profileImageView;
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

        // Setup Navigazione
        backBtn.setOnAction(e -> SceneManager.changeScene(e, "Home.fxml", 800, 600, true));
        homeBtn.setOnAction(e -> SceneManager.changeScene(e, "Home.fxml", 800, 600, true));
        cancelBtn.setOnAction(e -> SceneManager.changeScene(e, "Home.fxml", 800, 600, true));

        logoutBtn.setOnAction(e -> {
            UserSession.getInstance().cleanUserSession();
            SceneManager.changeScene(e, "Autenticazione.fxml", 600, 500, false);
        });

        genreCombo.getItems().addAll("Nessuno", "Rock", "Pop", "Jazz", "Classica", "Hip Hop", "Indie");
        loadUserData();

        changePicBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                tempProfilePicPath = file.toURI().toString();
                profileImageView.setImage(new Image(tempProfilePicPath));
                centerImage();
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

            if (currentUser.getProfilePicPath() != null && !currentUser.getProfilePicPath().isEmpty()) {
                try {
                    profileImageView.setImage(new Image(currentUser.getProfilePicPath()));
                    tempProfilePicPath = currentUser.getProfilePicPath();
                } catch (Exception e) {
                    setAnonymousImage();
                }
            } else {
                setAnonymousImage();
            }
            centerImage();
        }
    }

    private void setAnonymousImage() {
        try {
            URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
            if (imageUrl != null) {
                profileImageView.setImage(new Image(imageUrl.toExternalForm()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void centerImage() {
        Rectangle clip = new Rectangle(140, 140);
        clip.setArcWidth(140);
        clip.setArcHeight(140);
        profileImageView.setClip(clip);
    }

    private void saveChanges(ActionEvent event) {
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