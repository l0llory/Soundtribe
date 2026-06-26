package com.example.soundtribe.controller;

import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.dao.*;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.entita.Execution;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    @FXML public Button handleMyComments;

    @FXML public VBox activitiesContainer;

    public void initialize(){
        NavigationManager.updateNavigationButtons(precP, nextP);

        setupProfileButton();
        profileButton.setOnAction(event -> SceneManager.changeScene(event, "GestioneProfilo.fxml", true));

        globalSearchBtn.setOnAction(this::performGlobalSearch);
        globalSearchField.setOnAction(this::performGlobalSearch);

        handleBraniMusicali.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", true));
        handleUtenti.setOnAction(event -> SceneManager.changeScene(event, "gestioneUtenti.fxml", true));
        handleCaricaMateriale.setOnAction(event -> SceneManager.changeScene(event, "caricaMateriale.fxml", true));
        handleAmministrazione.setOnAction(e -> {
            if (UserSession.getInstance().isAdmin()) {
                SceneManager.changeScene(e, "Amministrazione.fxml", true);
            } else {
                SceneManager.changeScene(e, "AmministrazioneUtente.fxml", true);
            }
        });

        handleMyComments.setOnAction(event -> {
            UserSession.getInstance().setLastSearchQuery("filter:commented_by_me");
            SceneManager.changeScene(event, "braniMusicali.fxml", true);
        });

        loadRecentActivities();
    }

    private void loadRecentActivities() {
        // Query UNION ordinata per created_at DESC: garantisce ordine cronologico reale
        // tra tabelle con sequenze ID indipendenti
        String sql =
            "SELECT 'NEW_SONG' AS type, s.id, " +
            "('Nuovo Brano: ' || s.title) AS title, " +
            "('Artista: ' || COALESCE(s.artist, 'Sconosciuto')) AS detail, " +
            "s.created_at FROM songs s " +
            "UNION ALL " +
            "SELECT 'NEW_EXECUTION' AS type, m.id, " +
            "COALESCE(NULLIF(m.title, ''), 'Esecuzione senza titolo') AS title, " +
            "CASE WHEN m.song_id IS NULL OR m.song_id = 0 " +
            "     THEN 'Inedito da: ' || COALESCE(u.name || ' ' || u.surname, 'Sconosciuto') " +
            "     ELSE 'Cover da: ' || COALESCE(u.name || ' ' || u.surname, 'Sconosciuto') " +
            "END AS detail, " +
            "m.created_at FROM media_files m LEFT JOIN users u ON m.uploader_id = u.id " +
            "UNION ALL " +
            "SELECT 'NEW_USER' AS type, u.id, " +
            "('Benvenuto ' || u.name || '!') AS title, " +
            "'Nuovo membro della community' AS detail, " +
            "u.created_at FROM users u WHERE u.status = 'Verified' " +
            "UNION ALL " +
            "SELECT 'NEW_CONCERT' AS type, c.id, " +
            "('Concerto: ' || c.title) AS title, " +
            "('Artista: ' || COALESCE(c.artist, 'Sconosciuto')) AS detail, " +
            "c.created_at FROM concerts c " +
            "ORDER BY created_at DESC NULLS LAST " +
            "LIMIT 20";

        activitiesContainer.getChildren().clear();
        try (Connection conn = CredDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RecentActivity.Type type = RecentActivity.Type.valueOf(rs.getString("type"));
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                RecentActivity activity = new RecentActivity(
                    type,
                    rs.getString("title"),
                    rs.getString("detail"),
                    rs.getInt("id"),
                    createdAt
                );
                activitiesContainer.getChildren().add(buildActivityCard(activity));
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento attività recenti: " + e.getMessage());
        }
    }

    private HBox buildActivityCard(RecentActivity item) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(
            "-fx-background-color: #252525;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #333;" +
            "-fx-border-width: 1;"
        );

        Label iconLabel = new Label();
        iconLabel.setStyle("-fx-font-size: 22px;");
        switch (item.getType()) {
            case NEW_SONG:      iconLabel.setText("🎵"); break;
            case NEW_EXECUTION: iconLabel.setText("🎤"); break;
            case NEW_USER:      iconLabel.setText("👋"); break;
            case NEW_CONCERT:   iconLabel.setText("🎭"); break;
        }

        VBox textBox = new VBox(2);
        Label titleLbl = new Label(item.getTitle());
        titleLbl.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label detailLbl = new Label(item.getDetail());
        detailLbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        textBox.getChildren().addAll(titleLbl, detailLbl);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        VBox rightBox = new VBox(3);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        Label typeTag = new Label(item.getTypeName());
        typeTag.setStyle(
            "-fx-text-fill: #666;" +
            "-fx-font-size: 9px;" +
            "-fx-border-color: #444;" +
            "-fx-border-radius: 4;" +
            "-fx-padding: 2 5 2 5;"
        );
        Label timeLbl = new Label(formatRelativeTime(item.getCreatedAt()));
        timeLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");
        rightBox.getChildren().addAll(typeTag, timeLbl);

        card.getChildren().addAll(iconLabel, textBox, rightBox);
        return card;
    }

    private String formatRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 1) return "Adesso";
        if (minutes < 60) return minutes + " min fa";
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + " h fa";
        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days == 1) return "Ieri";
        if (days < 7) return days + " giorni fa";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static class RecentActivity {
        public enum Type { NEW_SONG, NEW_EXECUTION, NEW_USER, NEW_CONCERT }

        private final Type type;
        private final String title;
        private final String detail;
        private final int id;
        private final LocalDateTime createdAt;

        public RecentActivity(Type type, String title, String detail, int id, LocalDateTime createdAt) {
            this.type = type;
            this.title = title;
            this.detail = detail;
            this.id = id;
            this.createdAt = createdAt;
        }

        public Type getType() { return type; }
        public String getTitle() { return title; }
        public String getDetail() { return detail; }
        public int getId() { return id; }
        public LocalDateTime getCreatedAt() { return createdAt; }

        public String getTypeName() {
            switch (type) {
                case NEW_SONG:     return "BRANO";
                case NEW_EXECUTION: return "ESECUZIONE";
                case NEW_USER:     return "UTENTE";
                case NEW_CONCERT:  return "CONCERTO";
                default:           return "";
            }
        }
    }

    private void performGlobalSearch(Event event) {
        String query = globalSearchField.getText().trim();
        if (query.isEmpty()) return;

        UserSession.getInstance().setLastSearchQuery(query);

        SongDAO songDAO = new SongDAO();
        ExecutionDAO execDAO = new ExecutionDAO();
        UserDAO userDAO = new UserDAO();

        List<Song> foundSongs = songDAO.searchSongs(query);
        List<Execution> foundExecutions = execDAO.searchExecutions(query);
        List<User> foundUsers = userDAO.searchUsers(query);

        if (!foundSongs.isEmpty() || !foundExecutions.isEmpty()) {
            goToScene(event, "braniMusicali.fxml");
        } else if (!foundUsers.isEmpty()) {
            UserSession.getInstance().setLastUserSearchQuery(query);
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
