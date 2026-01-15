package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.net.URL;

public class HomeController {
    @FXML public Button precP;
    @FXML public Button nextP;

    // Bottoni menu
    @FXML public Button handleBraniMusicali;
    @FXML public Button handleUtenti;
    @FXML public Button handleCommentiNote;
    @FXML public Button handleRicerca;
    @FXML public Button handleCaricaMateriale;
    @FXML public Button handleAmministrazione;

    // Bottone profilo
    @FXML public Button profileButton;

    public void initialize(){
        NavigationManager.updateNavigationButtons(precP, nextP);
        NavigationManager.navBack(precP);

        // Setup del bottone profilo (Carica immagine corretta)
        setupProfileButton();

        // Event Handler per navigazione al profilo
        profileButton.setOnAction(event -> {
            SceneManager.changeScene(event, "GestioneProfilo.fxml", 800, 600, true);
        });

        handleBraniMusicali.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true));
        handleUtenti.setOnAction(event -> SceneManager.changeScene(event, "gestioneUtenti.fxml", 800, 600, true));
        handleRicerca.setOnAction(event -> SceneManager.changeScene(event, "Ricerca.fxml", 800, 600, true));
        handleCommentiNote.setOnAction(event -> SceneManager.changeScene(event, "commentiENote.fxml", 800, 600, true));
        handleCaricaMateriale.setOnAction(event -> SceneManager.changeScene(event, "caricaMateriale.fxml", 800, 600, true));
        handleAmministrazione.setOnAction(event -> SceneManager.changeScene(event, "Amministrazione.fxml", 800, 600, true));
    }

    private void setupProfileButton() {
        // 1. Recupera l'utente corrente dal DB per avere i dati freschi (es. foto appena cambiata)
        int userId = UserSession.getInstance().getUserId();
        UserDAO userDAO = new UserDAO();
        User currentUser = userDAO.getUserById(userId);

        Circle circle = new Circle(20); // Raggio del cerchio interno

        // 2. Controllo se esiste il path della foto
        if (currentUser != null && currentUser.getProfilePicPath() != null && !currentUser.getProfilePicPath().isEmpty()) {
            try {
                // Tenta di caricare l'immagine dal percorso salvato
                Image img = new Image(currentUser.getProfilePicPath());

                if (img.isError()) {
                    // Se il file è corrotto o spostato, usa default
                    setAnonymousIcon(circle);
                } else {
                    circle.setFill(new ImagePattern(img));
                }
            } catch (Exception e) {
                // Se c'è un errore nel percorso, usa default
                setAnonymousIcon(circle);
            }
        } else {
            // Nessuna foto nel DB -> usa default
            setAnonymousIcon(circle);
        }

        profileButton.setGraphic(circle);
    }

    private void setAnonymousIcon(Circle circle) {
        try {
            // Carica l'immagine dalle risorse
            URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");

            if (imageUrl != null) {
                Image img = new Image(imageUrl.toExternalForm());
                circle.setFill(new ImagePattern(img));
            } else {
                // Fallback estremo se manca il file png
                circle.setFill(Color.LIGHTGRAY);
                circle.setStroke(Color.DARKGRAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            circle.setFill(Color.LIGHTGRAY);
        }
    }
}