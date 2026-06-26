package com.example.soundtribe.controller;

import com.example.soundtribe.entita.Concert;
import com.example.soundtribe.dao.*;
import com.example.soundtribe.entita.MusicItem;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.entita.Execution;
import com.example.soundtribe.entita.Song;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    @FXML public Button btnDizionari;
    @FXML public TextField searchBarSongs;

    @FXML public ComboBox<String> generiFilter;
    @FXML public ComboBox<String> strumentiFilter; // NUOVO APPOSITO MENU A TENDINA PER GLI STRUMENTI

    @FXML public ListView<MusicItem> listaBrani;

    private SongDAO songDAO;
    private ExecutionDAO execDAO;
    private ConcertDAO concertDAO;
    private List<MusicItem> allMusicItems;

    private boolean showOnlyCommented = false;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();
        execDAO = new ExecutionDAO();
        concertDAO = new ConcertDAO();
        allMusicItems = new ArrayList<>();

        // 1. POPOLAMENTO MENU GENERI DAL DIZIONARIO
        generiFilter.getItems().clear();
        generiFilter.getItems().add("Tutti");
        generiFilter.getItems().addAll(songDAO.getDistinctGenres());
        generiFilter.getItems().addAll("Esecuzioni", "Concerti");
        generiFilter.getSelectionModel().selectFirst();

        // 2. POPOLAMENTO NUOVO MENU STRUMENTI DAL DIZIONARIO
        if (strumentiFilter != null) {
            strumentiFilter.getItems().clear();
            strumentiFilter.getItems().add("Tutti");
            // Estrae l'elenco dei nomi reali degli strumenti
            List<String> listInst = execDAO.getDistinctInstruments();
            strumentiFilter.getItems().addAll(listInst);
            strumentiFilter.getSelectionModel().selectFirst();
        }

        // Setup Navigazione
        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

        setupCellFactory();

        // 3. INTERCETTAZIONE E SMISTAMENTO DEI FILTRI DAL DIZIONARIO (O BARRA GLOBALE)
        String pendingSearch = UserSession.getInstance().getLastSearchQuery();

        if ("filter:commented_by_me".equals(pendingSearch)) {
            showOnlyCommented = true;
            loadCommentedSongsOnly();
            UserSession.getInstance().setLastSearchQuery(null);
            searchBarSongs.setPromptText("Visualizzando i brani commentati da te");
        } else {
            showOnlyCommented = false;
            loadAllData(); // Carica esecuzioni e concerti in memoria

            if (pendingSearch != null && !pendingSearch.isEmpty()) {
                if (pendingSearch.startsWith("genre:")) {
                    // Smista nel menu a tendina dei generi
                    generiFilter.setValue(pendingSearch.substring(6));
                } else if (pendingSearch.startsWith("instrument:")) {
                    // Smista nel menu a tendina degli strumenti
                    if (strumentiFilter != null) {
                        strumentiFilter.setValue(pendingSearch.substring(11));
                    }
                } else {
                    // Se è un titolo o un autore rimasto puro, lo mette come testo libero
                    searchBarSongs.setText(pendingSearch);
                }
                UserSession.getInstance().setLastSearchQuery(null);
            }
        }

        applyFilters();

        // 4. LISTENERS AGGIORNATI CON IL NUOVO FILTRO STRUMENTI
        searchBarSongs.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        generiFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        if (strumentiFilter != null) {
            strumentiFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        aggiungiBrano.setOnAction(e -> SceneManager.changeScene(e, "aggiungiBrano.fxml", true));
        if(btnDizionari != null) {
            btnDizionari.setOnAction(e -> SceneManager.changeScene(e, "dizionari.fxml", true));
        }
    }

    private void loadAllData() {
        allMusicItems.clear();
        List<Execution> executions = execDAO.getAllExecutions();
        for (Execution e : executions) allMusicItems.add(new MusicItem(e));
        List<Concert> concerts = concertDAO.getAllConcerts();
        for (Concert c : concerts) allMusicItems.add(new MusicItem(c));
    }

    private void loadCommentedSongsOnly() {
        allMusicItems.clear();
        int currentUserId = UserSession.getInstance().getUserId();
        List<Song> commentedSongs = songDAO.getSongsCommentedByUser(currentUserId);
        for (Song s : commentedSongs) allMusicItems.add(new MusicItem(s));
    }

    // --- LOGICA DI RICERCA TOTALMENTE OTTIMIZZATA PER LE TRE ENTITÀ ---
    private void applyFilters() {
        String query = searchBarSongs.getText().toLowerCase().trim();
        String selectedGenre = generiFilter.getValue();
        String selectedInstrument = (strumentiFilter != null) ? strumentiFilter.getValue() : "Tutti";

        List<MusicItem> filteredList = new ArrayList<>();

        if (showOnlyCommented) {
            // Modalità ristretta "I miei commenti"
            filteredList = allMusicItems.stream()
                    .filter(item -> {
                        boolean matchesText = query.isEmpty() ||
                                item.getTitle().toLowerCase().contains(query) ||
                                item.getArtist().toLowerCase().contains(query);
                        boolean matchesGenre = selectedGenre == null || selectedGenre.equals("Tutti") ||
                                (item.isSong() && item.getSong().getGenre().equalsIgnoreCase(selectedGenre));
                        return matchesText && matchesGenre;
                    })
                    .collect(Collectors.toList());
        } else {
            // 1. CARICAMENTO E RICERCA DEI BRANI (SONGS) DAL DATABASE
            // Un brano (Song) viene mostrato solo se non stiamo filtrando specificatamente per categorie "Esecuzioni" o "Concerti",
            // e solo se non stiamo filtrando per uno specifico strumento (visto che i brani strutturalmente non li hanno)
            if ((selectedInstrument == null || selectedInstrument.equals("Tutti")) &&
                    (selectedGenre == null || selectedGenre.equals("Tutti") ||
                            (!selectedGenre.equals("Esecuzioni") && !selectedGenre.equals("Concerti")))) {

                String genreForDb = (selectedGenre != null && !selectedGenre.equals("Tutti")) ? selectedGenre : null;
                // La query avanzata ora cerca la stringa sia nel Titolo che nell'Autore del Brano
                List<Song> dbSongs = songDAO.searchSongsAdvanced(query, null, genreForDb);
                for (Song s : dbSongs) {
                    filteredList.add(new MusicItem(s));
                }
            }

            // 2. RICERCA E TRACCIAMENTO SU ESECUZIONI E CONCERTI IN MEMORIA
            if (allMusicItems != null) {
                allMusicItems.stream()
                        .filter(item -> {
                            if (item.isSong()) return false; // Già estratte via DB sopra

                            // Ricerca testuale per titolo, artista o strumenti scritti nella riga
                            boolean matchesText = query.isEmpty() ||
                                    item.getTitle().toLowerCase().contains(query) ||
                                    item.getArtist().toLowerCase().contains(query) ||
                                    (item.isExecution() && item.getExecution().getInstruments() != null && item.getExecution().getInstruments().toLowerCase().contains(query));

                            // Controllo filtri di Genere / Categoria
                            boolean matchesGenre = true;
                            if (selectedGenre != null && !selectedGenre.equals("Tutti")) {
                                if (selectedGenre.equals("Esecuzioni")) matchesGenre = item.isExecution();
                                else if (selectedGenre.equals("Concerti")) matchesGenre = item.isConcert();
                                else {
                                    // Se viene selezionato un genere specifico (es. Pop), verifichiamo se l'esecuzione è legata a una canzone di quel genere
                                    if (item.isExecution() && item.getExecution().getSongId() > 0) {
                                        Song linked = songDAO.getSongById(item.getExecution().getSongId());
                                        matchesGenre = (linked != null && linked.getGenre() != null && linked.getGenre().equalsIgnoreCase(selectedGenre));
                                    } else {
                                        matchesGenre = false; // I concerti o gli inediti puri non hanno genere fisso
                                    }
                                }
                            }

                            // Controllo filtri del menu a tendina degli Strumenti Musicali
                            boolean matchesInstrument = true;
                            if (selectedInstrument != null && !selectedInstrument.equals("Tutti")) {
                                if (item.isExecution()) {
                                    matchesInstrument = item.getExecution().getInstruments() != null &&
                                            item.getExecution().getInstruments().toLowerCase().contains(selectedInstrument.toLowerCase());
                                } else if (item.isConcert()) {
                                    // Verifica se lo strumento è menzionato nei dettagli/scaletta del concerto
                                    matchesInstrument = item.getConcert().getDescription() != null &&
                                            item.getConcert().getDescription().toLowerCase().contains(selectedInstrument.toLowerCase());
                                } else {
                                    matchesInstrument = false;
                                }
                            }

                            return matchesText && matchesGenre && matchesInstrument;
                        })
                        .forEach(filteredList::add);
            }
        }

        // Ordina il risultato unificato alfabeticamente per Titolo
        filteredList.sort(Comparator.comparing(MusicItem::getTitle, String.CASE_INSENSITIVE_ORDER));
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
                        try {
                            FXMLLoader loader = new FXMLLoader();
                            if (item.isSong()) {
                                NavigationManager.navigateTo("esploraBrani.fxml");
                                loader.setLocation(getClass().getResource("/com/example/soundtribe/view/esploraBrani.fxml"));
                            }
                            else if (item.isExecution()) {
                                NavigationManager.navigateTo("esploraEsecuzione.fxml");
                                loader.setLocation(getClass().getResource("/com/example/soundtribe/view/esploraEsecuzione.fxml"));
                            }
                            else {
                                NavigationManager.navigateTo("esploraConcerto.fxml");
                                loader.setLocation(getClass().getResource("/com/example/soundtribe/view/esploraConcerto.fxml"));
                            }

                            Parent root = loader.load();

                            if (item.isSong()) {
                                ExploreSongController controller = loader.getController();
                                controller.setSong(item.getSong());
                            } else if (item.isExecution()) {
                                ExploreExecutionController controller = loader.getController();
                                controller.setExecution(item.getExecution());
                            } else {
                                ExploreConcertController controller = loader.getController();
                                controller.setConcert(item.getConcert());
                            }

                            Scene currentScene = ((Node) e.getSource()).getScene();
                            Stage stage = (Stage) currentScene.getWindow();

                            double width = currentScene.getWidth();
                            double height = currentScene.getHeight();
                            boolean isMaximized = stage.isMaximized();

                            Scene newScene = new Scene(root, width, height);
                            newScene.getStylesheets().add(SceneManager.class.getResource("/com/example/soundtribe/css/style.css").toExternalForm());

                            stage.setScene(newScene);

                            if (isMaximized) {
                                stage.setMaximized(true);
                            }

                            stage.show();

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });

                    actionBox.getChildren().add(exploreBtn);
                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card); setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
    }

    private Node createSongIcon(Song song) { return createAvatar("🎵", Color.web("#3969da")); }
    private Node createExecutionIcon() { return createAvatar("🎤", Color.web("#da3969")); }
    private Node createConcertIcon() { return createAvatar("🎸", Color.web("#8a2be2")); }

    private Node createAvatar(String emoji, Color bgColor) {
        StackPane stack = new StackPane();
        Circle bg = new Circle(25); bg.setFill(bgColor);
        Label icon = new Label(emoji); icon.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        stack.getChildren().addAll(bg, icon); return stack;
    }
}