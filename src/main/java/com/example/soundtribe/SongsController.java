package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.entità.Song;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SongsController {

    @FXML public Button precP_Songs;
    @FXML public Button nextP_Songs;
    @FXML public Button backHome_Songs;
    @FXML public Button Exit_Songs;
    @FXML public Button aggiungiBrano;
    @FXML public TextField searchBarSongs;
    @FXML public ComboBox<String> generiFilter;
    @FXML public ListView<Song> listaBrani;

    private SongDAO songDAO;
    private List<Song> allSongsMasterList;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();

        // 1. POPOLAMENTO MENU GENERI
        generiFilter.getItems().add("Tutti i generi");
        generiFilter.getItems().addAll(
                "Afro", "Blues", "Folk", "Indie", "Jazz", "Musica classica",
                "Pop", "Raggae", "Rap", "Reggetton", "Rock", "Trap"
        );
        generiFilter.getSelectionModel().selectFirst();

        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.navBack(precP_Songs);
        NavigationManager.navForward(nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

        setupCellFactory();

        // 2. Carichiamo tutti i brani
        allSongsMasterList = songDAO.getAllSongs();
        applyFilters(); // Mostra tutto all'inizio

        aggiungiBrano.setOnAction(event -> SceneManager.changeScene(event, "aggiungiBrano.fxml", 800, 600, true));

        // 3. LISTENERS: Aggiornano la lista in tempo reale
        searchBarSongs.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        generiFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // 4. GESTIONE RICERCA DALLA HOME
        String pendingSearch = UserSession.getInstance().getLastSearchQuery();
        if (pendingSearch != null && !pendingSearch.isEmpty()) {
            searchBarSongs.setText(pendingSearch); // Questo triggera automaticamente il listener sopra!
            UserSession.getInstance().setLastSearchQuery(null); // Pulisci sessione
        }
    }

    private void applyFilters() {
        if (allSongsMasterList == null) return;

        String query = searchBarSongs.getText().toLowerCase().trim();
        String selectedGenre = generiFilter.getValue();

        List<Song> filteredList = allSongsMasterList.stream()
                .filter(song -> {
                    boolean matchesText = query.isEmpty() ||
                            song.getTitle().toLowerCase().contains(query) ||
                            song.getArtist().toLowerCase().contains(query);

                    boolean matchesGenre = selectedGenre == null ||
                            selectedGenre.equals("Tutti i generi") ||
                            song.getGenre().equalsIgnoreCase(selectedGenre);

                    return matchesText && matchesGenre;
                })
                .sorted(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        listaBrani.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void setupCellFactory() {
        listaBrani.setCellFactory(param -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);

                if (empty || song == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox card = new VBox(10);
                    card.setPadding(new Insets(15));
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #3969da; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

                    HBox header = new HBox(15);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Node visualElement;
                    if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
                        try {
                            ImageView coverImg = new ImageView(new Image(song.getCoverPath()));
                            coverImg.setFitWidth(50);
                            coverImg.setFitHeight(50);
                            coverImg.setPreserveRatio(true);
                            Rectangle clip = new Rectangle(50, 50);
                            clip.setArcWidth(10);
                            clip.setArcHeight(10);
                            coverImg.setClip(clip);
                            visualElement = coverImg;
                        } catch (Exception e) {
                            visualElement = createDefaultIcon();
                        }
                    } else {
                        visualElement = createDefaultIcon();
                    }

                    Label titleLabel = new Label(song.getTitle());
                    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                    titleLabel.setTextFill(javafx.scene.paint.Color.web("#3969da"));

                    Label genreTag = new Label(song.getGenre());
                    genreTag.setStyle("-fx-background-color: #f0f4ff; -fx-text-fill: #3969da; -fx-padding: 2 10; -fx-background-radius: 15; -fx-border-color: #3969da; -fx-border-radius: 15;");
                    genreTag.setFont(Font.font(10));

                    HBox titleGroup = new HBox(10, titleLabel, genreTag);
                    titleGroup.setAlignment(Pos.CENTER_LEFT);

                    header.getChildren().addAll(visualElement, titleGroup);

                    Label authorLabel = new Label("Autore: " + song.getArtist());
                    authorLabel.setTextFill(javafx.scene.paint.Color.web("#908888"));
                    authorLabel.setPadding(new Insets(0, 0, 0, 65));

                    HBox actionBox = new HBox(10);
                    actionBox.setPadding(new Insets(5, 0, 0, 65));

                    Button playBtn = new Button("▶ Esplora");
                    playBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                    playBtn.setOnAction(e -> openSongDetails(song, playBtn));
                    actionBox.getChildren().add(playBtn);

                    if (song.getYoutubeUrl() != null && !song.getYoutubeUrl().isEmpty()) {
                        Button ytBtn = createStyledLink("📺 YouTube");
                        ytBtn.setOnAction(e -> openUrlSafe(song.getYoutubeUrl()));
                        actionBox.getChildren().add(ytBtn);
                    }

                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }

            private Label createDefaultIcon() {
                Label musicIcon = new Label("♫");
                musicIcon.setTextFill(javafx.scene.paint.Color.web("#3969da"));
                musicIcon.setFont(Font.font("System", FontWeight.BOLD, 24));
                musicIcon.setMinWidth(50);
                musicIcon.setAlignment(Pos.CENTER);
                return musicIcon;
            }

            private Button createStyledLink(String text) {
                Button btn = new Button(text);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3969da; -fx-font-size: 11px; -fx-border-color: #3969da; -fx-border-radius: 5; -fx-cursor: hand;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3969da; -fx-font-size: 11px; -fx-border-color: #3969da; -fx-border-radius: 5; -fx-cursor: hand;"));
                return btn;
            }
        });
    }

    private void openSongDetails(Song song, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("esploraBrani.fxml"));
            Parent root = loader.load();
            ExploreSongController controller = loader.getController();
            controller.setSong(song);
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUrlSafe(String url) {
        try {
            Launcher.getInstance().openDocument(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}