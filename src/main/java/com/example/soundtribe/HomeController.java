package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Song;
import com.example.soundtribe.entità.User;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.List;

public class HomeController {
    @FXML public Button precP;
    @FXML public Button nextP;

    // Barra di ricerca integrata
    @FXML public TextField globalSearchField;
    @FXML public Button globalSearchBtn;

    // Bottoni menu
    @FXML public Button handleBraniMusicali;
    @FXML public Button handleUtenti;
    @FXML public Button handleCaricaMateriale;
    @FXML public Button handleAmministrazione;

    // Bottone profilo
    @FXML public Button profileButton;

    public void initialize(){
        NavigationManager.updateNavigationButtons(precP, nextP);
        NavigationManager.navBack(precP);

        // Setup profilo
        setupProfileButton();
        profileButton.setOnAction(event -> {
            SceneManager.changeScene(event, "GestioneProfilo.fxml", 800, 600, true);
        });

        // Setup barra di ricerca (Funziona sia col click che con Invio)
        globalSearchBtn.setOnAction(event -> performGlobalSearch(event));
        globalSearchField.setOnAction(event -> performGlobalSearch(event));

        // Navigazione Menu
        handleBraniMusicali.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true));
        handleUtenti.setOnAction(event -> SceneManager.changeScene(event, "gestioneUtenti.fxml", 800, 600, true));
        handleCaricaMateriale.setOnAction(event -> SceneManager.changeScene(event, "caricaMateriale.fxml", 800, 600, true));
        handleAmministrazione.setOnAction(event -> SceneManager.changeScene(event, "Amministrazione.fxml", 800, 600, true));
    }

    private void performGlobalSearch(Event event) {
        String query = globalSearchField.getText().trim();

        // Se la ricerca è vuota, non fare nulla
        if (query.isEmpty()) return;

        System.out.println("Ricerca globale per: " + query);

        // 1. Salviamo la query nella sessione per passarla alla prossima schermata
        UserSession.getInstance().setLastSearchQuery(query);

        // 2. Logica di Routing Intelligente
        SongDAO songDAO = new SongDAO();
        UserDAO userDAO = new UserDAO();

        // Controlliamo se ci sono canzoni che corrispondono
        List<Song> foundSongs = songDAO.searchSongs(query);

        if (!foundSongs.isEmpty()) {
            // Priorità ai brani: se ne trovo, vado alla lista brani
            goToScene(event, "braniMusicali.fxml");
        } else {
            // Se non trovo brani, cerco tra gli utenti
            List<User> foundUsers = userDAO.searchUsers(query);
            if (!foundUsers.isEmpty()) {
                // Trovato utente/i: vado alla gestione utenti
                goToScene(event, "gestioneUtenti.fxml");
            } else {
                // Se non trovo nulla, di default vado ai brani (che mostrerà lista vuota o messaggio)
                goToScene(event, "braniMusicali.fxml");
            }
        }
    }

    // Metodo helper per cambiare scena in modo pulito
    private void goToScene(Event sourceEvent, String fxmlFile) {
        // Se l'evento proviene da un tasto Invio (TextField), potrebbe non essere direttamente usabile da SceneManager
        // Creiamo un evento ActionEvent pulito basato sul bottone di ricerca per sicurezza
        ActionEvent cleanEvent = new ActionEvent(globalSearchBtn, Event.NULL_SOURCE_TARGET);
        SceneManager.changeScene(cleanEvent, fxmlFile, 800, 600, true);
    }

    private void setupProfileButton() {
        int userId = UserSession.getInstance().getUserId();
        UserDAO userDAO = new UserDAO();
        User currentUser = userDAO.getUserById(userId);

        Circle circle = new Circle(20);

        if (currentUser != null && currentUser.getProfilePicPath() != null && !currentUser.getProfilePicPath().isEmpty()) {
            try {
                Image img = new Image(currentUser.getProfilePicPath());
                if (img.isError()) {
                    setAnonymousIcon(circle);
                } else {
                    circle.setFill(new ImagePattern(img));
                }
            } catch (Exception e) {
                setAnonymousIcon(circle);
            }
        } else {
            setAnonymousIcon(circle);
        }

        profileButton.setGraphic(circle);
    }

    private void setAnonymousIcon(Circle circle) {
        try {
            URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
            if (imageUrl != null) {
                Image img = new Image(imageUrl.toExternalForm());
                circle.setFill(new ImagePattern(img));
            } else {
                circle.setFill(Color.LIGHTGRAY);
                circle.setStroke(Color.DARKGRAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            circle.setFill(Color.LIGHTGRAY);
        }
    }
}