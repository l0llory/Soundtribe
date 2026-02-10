package com.example.soundtribe;

import com.example.soundtribe.dao.EsecutionDAO;
import com.example.soundtribe.dao.SongDAO;
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
import javafx.scene.layout.VBox;
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
    @FXML public TextField searchBarSongs;
    @FXML public ComboBox<String> generiFilter;

    // NOTA: Cambiato il tipo generico della ListView in MusicItem
    @FXML public ListView<MusicItem> listaBrani;

    private SongDAO songDAO;
    private EsecutionDAO execDAO;
    private List<MusicItem> allMusicItems;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();
        execDAO = new EsecutionDAO();
        allMusicItems = new ArrayList<>();

        // 1. POPOLAMENTO MENU GENERI
        generiFilter.getItems().add("Tutti");
        generiFilter.getItems().addAll(
                "Afro", "Blues", "Folk", "Indie", "Jazz", "Musica classica",
                "Pop", "Raggae", "Rap", "Reggetton", "Rock", "Trap", "Esecuzioni" // Aggiunto filtro per sole esecuzioni
        );
        generiFilter.getSelectionModel().selectFirst();

        // 2. Setup Navigazione
        NavigationManager.navBack(precP_Songs);
        NavigationManager.navForward(nextP_Songs);
        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

        setupCellFactory();

        // 3. Carichiamo TUTTI i dati (Brani + Esecuzioni)
        loadAllData();

        // 4. Gestione Ricerca Iniziale (dalla Home)
        String pendingSearch = UserSession.getInstance().getLastSearchQuery();
        if (pendingSearch != null && !pendingSearch.isEmpty()) {
            searchBarSongs.setText(pendingSearch);
            UserSession.getInstance().setLastSearchQuery(null); // Consuma la ricerca
        }

        applyFilters();

        // 5. LISTENERS
        searchBarSongs.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        generiFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 6. Aggiungi Brano
        aggiungiBrano.setOnAction(e -> SceneManager.changeScene(e, "aggiungiBrano.fxml", 800, 600, true));
    }

    private void loadAllData() {
        allMusicItems.clear();

        // Aggiungi Canzoni
        List<Song> songs = songDAO.getAllSongs();
        for (Song s : songs) {
            allMusicItems.add(new MusicItem(s));
        }

        // Aggiungi Esecuzioni
        List<Esecution> executions = execDAO.getAllExecutions();
        for (Esecution e : executions) {
            allMusicItems.add(new MusicItem(e));
        }
    }

    private void applyFilters() {
        if (allMusicItems == null) return;

        String query = searchBarSongs.getText().toLowerCase().trim();
        String selectedGenre = generiFilter.getValue();

        List<MusicItem> filteredList = allMusicItems.stream()
                .filter(item -> {
                    // Filtro Testo (Titolo o Artista)
                    boolean matchesText = query.isEmpty() ||
                            item.getTitle().toLowerCase().contains(query) ||
                            item.getArtist().toLowerCase().contains(query);

                    // Filtro Genere / Tipo
                    boolean matchesGenre = true;
                    if (selectedGenre != null && !selectedGenre.equals("Tutti")) {
                        if (selectedGenre.equals("Esecuzioni")) {
                            matchesGenre = !item.isSong(); // Mostra solo esecuzioni
                        } else {
                            // Mostra solo canzoni di quel genere
                            matchesGenre = item.isSong() && item.getSong().getGenre().equalsIgnoreCase(selectedGenre);
                        }
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
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Costruzione Card
                    VBox card = new VBox(10);
                    card.setPadding(new Insets(15));
                    card.getStyleClass().add("st-card");

                    // Header
                    HBox header = new HBox(15);
                    header.setAlignment(Pos.CENTER_LEFT);

                    // Icona / Immagine
                    Node iconNode;
                    if (item.isSong()) {
                        // Logica Immagine Canzone (come prima)
                        iconNode = createSongIcon(item.getSong());
                    } else {
                        // Icona per Esecuzione (Microfono o Nota diversa)
                        Label execIcon = new Label("🎤");
                        execIcon.setStyle("-fx-font-size: 28px; -fx-text-fill: -st-blue-primary;");
                        execIcon.setMinWidth(50);
                        execIcon.setAlignment(Pos.CENTER);
                        iconNode = execIcon;
                    }

                    // Titolo e Badge
                    VBox titleBox = new VBox(2);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.getStyleClass().add("st-label-blue");
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                    Label subTitleLabel;
                    if (item.isSong()) {
                        subTitleLabel = new Label(item.getSong().getGenre());
                        subTitleLabel.getStyleClass().add("st-badge");
                    } else {
                        // Se è esecuzione, mostra se è Inedito o Cover
                        String type = (item.getExecution().getSongId() == 0) ? "Inedito" : "Cover";
                        subTitleLabel = new Label(type);
                        subTitleLabel.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px;");
                    }
                    titleBox.getChildren().addAll(titleLabel, subTitleLabel);

                    header.getChildren().addAll(iconNode, titleBox);

                    // Autore / Esecutore
                    Label authorLabel = new Label(item.isSong() ?
                            "Autore: " + item.getSong().getArtist() :
                            "Esecutori: " + item.getExecution().getExecutors());
                    authorLabel.getStyleClass().add("st-label-subtitle");

                    // Bottone Esplora
                    HBox actionBox = new HBox();
                    actionBox.setAlignment(Pos.CENTER_RIGHT);
                    Button exploreBtn = new Button("Esplora");
                    exploreBtn.getStyleClass().add("st-button-small");
                    exploreBtn.getStyleClass().add("st-button-primary");

                    exploreBtn.setOnAction(e -> {
                        if (item.isSong()) {
                            openSongDetails(item.getSong(), exploreBtn);
                        } else {
                            openExecutionDetails(item.getExecution(), exploreBtn);
                        }
                    });

                    actionBox.getChildren().add(exploreBtn);

                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
    }

    // Helper per creare l'icona della canzone
    private Node createSongIcon(Song song) {
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
                return coverImg;
            } catch (Exception e) { }
        }
        Label defaultIcon = new Label("🎵");
        defaultIcon.setStyle("-fx-font-size: 28px; -fx-text-fill: -st-blue-primary;");
        defaultIcon.setMinWidth(50);
        defaultIcon.setAlignment(Pos.CENTER);
        return defaultIcon;
    }

    // Navigazione Dettaglio Canzone
    private void openSongDetails(Song song, Button source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("esploraBrani.fxml"));
            Parent root = loader.load();

            // Importante: Assicurati che ExploreSongController esista e abbia setSong()
            // ExploreSongController controller = loader.getController();
            // controller.setSong(song);

            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/com/example/soundtribe/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Navigazione Dettaglio Esecuzione (NUOVO)
    private void openExecutionDetails(Esecution execution, Button source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("esploraEsecuzione.fxml"));
            Parent root = loader.load();

            ExploreExecutionController controller = loader.getController();
            controller.setExecution(execution); // Passiamo l'esecuzione al controller

            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/com/example/soundtribe/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }
}