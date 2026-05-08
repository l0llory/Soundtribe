package com.example.soundtribe;

import com.example.soundtribe.dao.ConcertDAO;
import com.example.soundtribe.dao.EsecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.entità.Concert;
import com.example.soundtribe.entità.Esecution;
import com.example.soundtribe.entità.Song;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SongsController {

    @FXML public Button precP_Songs;
    @FXML public Button nextP_Songs;
    @FXML public Button backHome_Songs;
    @FXML public Button Exit_Songs;
    @FXML public Button aggiungiBrano;
    @FXML public Button btnDizionari; // NUOVO BOTTONE
    @FXML public TextField searchBarSongs;
    @FXML public ComboBox<String> generiFilter;

    @FXML public ListView<MusicItem> listaBrani;

    private SongDAO songDAO;
    private EsecutionDAO execDAO;
    private ConcertDAO concertDAO;
    private List<MusicItem> allMusicItems;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();
        execDAO = new EsecutionDAO();
        concertDAO = new ConcertDAO();
        allMusicItems = new ArrayList<>();

        // 1. POPOLAMENTO MENU GENERI
        generiFilter.getItems().add("Tutti");
        generiFilter.getItems().addAll(
                "Afro", "Blues", "Folk", "Indie", "Jazz", "Musica classica",
                "Pop", "Raggae", "Rap", "Reggetton", "Rock", "Trap", "Esecuzioni", "Concerti"
        );
        generiFilter.getSelectionModel().selectFirst();

        // 2. Setup Navigazione
        NavigationManager.navBack(precP_Songs);
        NavigationManager.navForward(nextP_Songs);
        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

        setupCellFactory();

        // 3. GESTIONE CARICAMENTO DATI
        String pendingSearch = UserSession.getInstance().getLastSearchQuery();

        if ("filter:commented_by_me".equals(pendingSearch)) {
            loadCommentedSongsOnly();
            UserSession.getInstance().setLastSearchQuery(null);
            searchBarSongs.setPromptText("Visualizzando i brani commentati da te");
        } else {
            loadAllData();
            if (pendingSearch != null && !pendingSearch.isEmpty()) {
                searchBarSongs.setText(pendingSearch);
                UserSession.getInstance().setLastSearchQuery(null);
            }
        }

        applyFilters();

        // 4. LISTENERS
        searchBarSongs.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        generiFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 5. AZIONI BOTTONI
        aggiungiBrano.setOnAction(e -> SceneManager.changeScene(e, "aggiungiBrano.fxml", true));

        // NUOVA AZIONE: APRI DIZIONARI
        if(btnDizionari != null) {
            btnDizionari.setOnAction(e -> SceneManager.changeScene(e, "dizionari.fxml", true));
        }
    }

    // Carica TUTTO (Brani + Esecuzioni + Concerti)
    private void loadAllData() {
        allMusicItems.clear();
        List<Song> songs = songDAO.getAllSongs();
        for (Song s : songs) allMusicItems.add(new MusicItem(s));
        List<Esecution> executions = execDAO.getAllExecutions();
        for (Esecution e : executions) allMusicItems.add(new MusicItem(e));
        List<Concert> concerts = concertDAO.getAllConcerts();
        for (Concert c : concerts) allMusicItems.add(new MusicItem(c));
    }

    // Carica SOLO i brani commentati
    private void loadCommentedSongsOnly() {
        allMusicItems.clear();
        int currentUserId = UserSession.getInstance().getUserId();
        List<Song> commentedSongs = songDAO.getSongsCommentedByUser(currentUserId);
        for (Song s : commentedSongs) allMusicItems.add(new MusicItem(s));
    }

    private void applyFilters() {
        if (allMusicItems == null) return;
        String query = searchBarSongs.getText().toLowerCase().trim();
        String selectedGenre = generiFilter.getValue();

        List<MusicItem> filteredList = allMusicItems.stream()
                .filter(item -> {
                    boolean matchesText = query.isEmpty() ||
                            item.getTitle().toLowerCase().contains(query) ||
                            item.getArtist().toLowerCase().contains(query);

                    boolean matchesGenre = true;
                    if (selectedGenre != null && !selectedGenre.equals("Tutti")) {
                        if (selectedGenre.equals("Esecuzioni")) matchesGenre = item.isExecution();
                        else if (selectedGenre.equals("Concerti")) matchesGenre = item.isConcert();
                        else matchesGenre = item.isSong() && item.getSong().getGenre().equalsIgnoreCase(selectedGenre);
                    }
                    return matchesText && matchesGenre;
                })
                .sorted(Comparator.comparing(MusicItem::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        listaBrani.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void setupCellFactory() {
        listaBrani.setCellFactory(param -> new ListCell<MusicItem>() {
            @Override
            protected void updateItem(MusicItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null); setStyle("-fx-background-color: transparent;");
                } else {
                    VBox card = new VBox(10);
                    card.setPadding(new Insets(15));
                    card.getStyleClass().add("st-card");

                    HBox header = new HBox(15);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Node iconNode;
                    if (item.isSong()) iconNode = createSongIcon(item.getSong());
                    else if (item.isExecution()) iconNode = createExecutionIcon();
                    else iconNode = createConcertIcon();

                    VBox titleBox = new VBox(5);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.getStyleClass().add("st-label-blue");
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                    Label subTitleLabel;
                    if (item.isSong()) {
                        subTitleLabel = new Label(item.getSong().getGenre());
                        subTitleLabel.getStyleClass().add("st-badge");
                    } else if (item.isExecution()) {
                        String type = (item.getExecution().getSongId() == 0) ? "Inedito" : "Cover";
                        subTitleLabel = new Label(type);
                        subTitleLabel.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px;");
                    } else {
                        subTitleLabel = new Label("CONCERTO");
                        subTitleLabel.setStyle("-fx-background-color: #8a2be2; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");
                    }
                    titleBox.getChildren().addAll(titleLabel, subTitleLabel);
                    header.getChildren().addAll(iconNode, titleBox);

                    String authorText;
                    if (item.isSong()) authorText = "Autore: " + item.getSong().getArtist();
                    else if (item.isExecution()) authorText = "Esecutori: " + item.getExecution().getExecutors();
                    else authorText = "Artista: " + item.getConcert().getArtist();

                    Label authorLabel = new Label(authorText);
                    authorLabel.getStyleClass().add("st-label-subtitle");
                    authorLabel.setPadding(new Insets(0, 0, 0, 65));

                    HBox actionBox = new HBox();
                    actionBox.setAlignment(Pos.CENTER_RIGHT);
                    Button exploreBtn = new Button("Esplora");
                    exploreBtn.getStyleClass().add("st-button-small");
                    exploreBtn.getStyleClass().add("st-button-primary");

                    exploreBtn.setOnAction(e -> {
                        if (item.isSong()) SceneManager.changeScene(e, "esploraBrani.fxml", true);
                        else if (item.isExecution()) SceneManager.changeScene(e,"esploraEsecuzione.fxml", true);
                        else SceneManager.changeScene(e, "esploraConcerto.fxml", true);
                    });

                    actionBox.getChildren().add(exploreBtn);
                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card); setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
    }

    // --- HELPER ---
    private Node createSongIcon(Song song) { /* ... codice esistente ... */ return createAvatar("🎵", Color.web("#3969da")); }
    private Node createExecutionIcon() { return createAvatar("🎤", Color.web("#da3969")); }
    private Node createConcertIcon() { return createAvatar("🎸", Color.web("#8a2be2")); }

    private Node createAvatar(String emoji, Color bgColor) {
        StackPane stack = new StackPane();
        Circle bg = new Circle(25); bg.setFill(bgColor);
        Label icon = new Label(emoji); icon.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        stack.getChildren().addAll(bg, icon); return stack;
    }

}