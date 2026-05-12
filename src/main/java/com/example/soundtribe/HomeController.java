package com.example.soundtribe;

import com.example.soundtribe.dao.EsecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Esecution;
import com.example.soundtribe.entità.Song;
import com.example.soundtribe.entità.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeController {
    @FXML public Button precP;
    @FXML public Button nextP;
    @FXML public TextField globalSearchField;
    @FXML public Button globalSearchBtn;

    // Bottoni Menu Griglia
    @FXML public Button handleBraniMusicali;
    @FXML public Button handleUtenti;
    @FXML public Button handleCaricaMateriale;
    @FXML public Button handleAmministrazione;

    // Bottoni Top Bar
    @FXML public Button profileButton;
    @FXML public Button handleMyComments; // <-- Ora è nella top bar

    @FXML public ListView<RecentActivity> lastActivities;

    public void initialize(){
        NavigationManager.updateNavigationButtons(precP, nextP);
        NavigationManager.navBack(precP);

        setupProfileButton();
        profileButton.setOnAction(event -> SceneManager.changeScene(event, "GestioneProfilo.fxml", true));

        globalSearchBtn.setOnAction(this::performGlobalSearch);
        globalSearchField.setOnAction(this::performGlobalSearch);

        handleBraniMusicali.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", true));
        handleUtenti.setOnAction(event -> SceneManager.changeScene(event, "gestioneUtenti.fxml", true));
        handleCaricaMateriale.setOnAction(event -> SceneManager.changeScene(event, "caricaMateriale.fxml", true));
        handleAmministrazione.setOnAction(event -> SceneManager.changeScene(event, "Amministrazione.fxml", true));

        // AZIONE: Naviga ai brani commentati
        handleMyComments.setOnAction(event -> {
            // Impostiamo il filtro speciale nella sessione
            UserSession.getInstance().setLastSearchQuery("filter:commented_by_me");
            SceneManager.changeScene(event, "braniMusicali.fxml", true);
        });

        setupActivityList();
        loadRecentActivities();
    }

    private void setupActivityList() {
        lastActivities.setCellFactory(param -> new ListCell<RecentActivity>() {
            @Override
            protected void updateItem(RecentActivity item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox badge = new HBox(15);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    badge.setPadding(new Insets(10));
                    badge.setStyle("-fx-background-color: -st-dark-secondary; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #333;");

                    Label iconLabel = new Label();
                    iconLabel.setStyle("-fx-font-size: 24px;");
                    switch (item.getType()) {
                        case NEW_SONG:      iconLabel.setText("🎵"); break;
                        case NEW_EXECUTION: iconLabel.setText("🎤"); break;
                        case NEW_USER:      iconLabel.setText("👋"); break;
                    }

                    VBox textContainer = new VBox(3);
                    Label titleLbl = new Label(item.getTitle());
                    titleLbl.getStyleClass().add("st-label-blue");
                    titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label detailLbl = new Label(item.getDetail());
                    detailLbl.getStyleClass().add("st-label-subtitle");
                    detailLbl.setStyle("-fx-font-size: 12px;");

                    textContainer.getChildren().addAll(titleLbl, detailLbl);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label typeTag = new Label(item.getTypeName());
                    typeTag.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 2 6;");

                    badge.getChildren().addAll(iconLabel, textContainer, spacer, typeTag);

                    setGraphic(badge);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0 5 0;");
                }
            }
        });
    }

    private void loadRecentActivities() {
        SongDAO songDAO = new SongDAO();
        EsecutionDAO execDAO = new EsecutionDAO();
        UserDAO userDAO = new UserDAO();

        List<RecentActivity> allActivities = new ArrayList<>();

        // 1. CARICA ESECUZIONI
        List<Esecution> executions = execDAO.getAllExecutions();
        for (Esecution e : executions) {
            String title = (e.getTitle() != null && !e.getTitle().isEmpty()) ? e.getTitle() : "Esecuzione senza titolo";
            String detail;

            if (e.getSongId() == 0) {
                detail = "Inedito caricato da: " + getUploaderName(e.getUploaderId(), userDAO);
            } else {
                Song linkedSong = songDAO.getSongById(e.getSongId());
                String songTitle = (linkedSong != null) ? linkedSong.getTitle() : "Brano sconosciuto";
                detail = "Cover di: " + songTitle + " | Di: " + getUploaderName(e.getUploaderId(), userDAO);
            }

            allActivities.add(new RecentActivity(
                    RecentActivity.Type.NEW_EXECUTION,
                    title,
                    detail,
                    e.getId()
            ));
        }

        // 2. CARICA CANZONI
        List<Song> songs = songDAO.getAllSongs();
        for (Song s : songs) {
            allActivities.add(new RecentActivity(
                    RecentActivity.Type.NEW_SONG,
                    "Nuovo Brano: " + s.getTitle(),
                    "Artista: " + s.getArtist(),
                    s.getId()
            ));
        }

        // 3. CARICA UTENTI
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            allActivities.add(new RecentActivity(
                    RecentActivity.Type.NEW_USER,
                    "Benvenuto " + u.getName() + "!",
                    "Nuovo membro della community",
                    u.getId()
            ));
        }

        // Ordinamento inverso per ID (mostra i più recenti in alto)
        allActivities.sort(Comparator.comparingInt(RecentActivity::getId).reversed());

        int limit = Math.min(allActivities.size(), 20);
        ObservableList<RecentActivity> items = FXCollections.observableArrayList(allActivities.subList(0, limit));
        lastActivities.setItems(items);
    }

    private String getUploaderName(int userId, UserDAO dao) {
        if (userId == 0) return "Sconosciuto";
        User u = dao.getUserById(userId);
        return (u != null) ? u.getName() + " " + u.getSurname() : "Utente " + userId;
    }

    public static class RecentActivity {
        public enum Type { NEW_SONG, NEW_EXECUTION, NEW_USER }

        private final Type type;
        private final String title;
        private final String detail;
        private final int id;

        public RecentActivity(Type type, String title, String detail, int id) {
            this.type = type;
            this.title = title;
            this.detail = detail;
            this.id = id;
        }

        public Type getType() { return type; }
        public String getTitle() { return title; }
        public String getDetail() { return detail; }
        public int getId() { return id; }

        public String getTypeName() {
            switch (type) {
                case NEW_SONG: return "BRANO";
                case NEW_EXECUTION: return "ESECUZIONE";
                case NEW_USER: return "UTENTE";
                default: return "";
            }
        }
    }

    private void performGlobalSearch(Event event) {
        String query = globalSearchField.getText().trim();
        if (query.isEmpty()) return;

        UserSession.getInstance().setLastSearchQuery(query);

        SongDAO songDAO = new SongDAO();
        EsecutionDAO execDAO = new EsecutionDAO();
        UserDAO userDAO = new UserDAO();

        List<Song> foundSongs = songDAO.searchSongs(query);
        List<Esecution> foundExecutions = execDAO.searchExecutions(query);
        List<User> foundUsers = userDAO.searchUsers(query);

        if (!foundSongs.isEmpty() || !foundExecutions.isEmpty()) {
            goToScene(event, "braniMusicali.fxml");
        } else if (!foundUsers.isEmpty()) {
            goToScene(event, "gestioneUtenti.fxml");
        } else {
            goToScene(event, "braniMusicali.fxml");
        }
    }

    private void goToScene(Event sourceEvent, String fxmlFile) {
        ActionEvent cleanEvent = new ActionEvent(globalSearchBtn, Event.NULL_SOURCE_TARGET);
        SceneManager.changeScene(cleanEvent, fxmlFile, true);
    }

    private void setupProfileButton() {
        int userId = UserSession.getInstance().getUserId();
        UserDAO userDAO = new UserDAO();
        User currentUser = userDAO.getUserById(userId);
        Circle circle = new Circle(20);
        if (currentUser != null && currentUser.getProfilePicPath() != null && !currentUser.getProfilePicPath().isEmpty()) {
            try {
                Image img = new Image(currentUser.getProfilePicPath());
                if (img.isError()) setAnonymousIcon(circle);
                else circle.setFill(new ImagePattern(img));
            } catch (Exception e) { setAnonymousIcon(circle); }
        } else { setAnonymousIcon(circle); }
        profileButton.setGraphic(circle);
    }

    private void setAnonymousIcon(Circle circle) {
        try {
            URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
            if (imageUrl != null) circle.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
            else { circle.setFill(Color.LIGHTGRAY); circle.setStroke(Color.DARKGRAY); }
        } catch (Exception e) { circle.setFill(Color.LIGHTGRAY); }
    }
}