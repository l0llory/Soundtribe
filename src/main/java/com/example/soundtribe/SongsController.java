package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Comparator;

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

    @FXML
    public void initialize() {
        songDAO = new SongDAO();

        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.navBack(precP_Songs);
        NavigationManager.navForward(nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

        // Custom Cell Factory per lo stile Figma
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
                    card.setStyle("-fx-background-color: #252545; -fx-background-radius: 10; -fx-border-color: #3969da; -fx-border-radius: 10;");

                    // Header: Icona + Titolo + Genere + Anno
                    HBox header = new HBox(10);
                    Label musicIcon = new Label("♫");
                    musicIcon.setTextFill(javafx.scene.paint.Color.web("#3969da"));

                    Label titleLabel = new Label(song.getTitle());
                    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                    titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);

                    Label genreTag = new Label(song.getGenre());
                    genreTag.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 5;");
                    genreTag.setFont(Font.font(10));

                    header.getChildren().addAll(musicIcon, titleLabel, genreTag);

                    // Autore
                    Label authorLabel = new Label("Autore: " + song.getArtist());
                    authorLabel.setTextFill(javafx.scene.paint.Color.web("#908888"));

                    // Buttons/Links Section
                    HBox actionBox = new HBox(10);
                    actionBox.setPadding(new Insets(5, 0, 0, 0));

                    if (song.getPdfSheetPath() != null && !song.getPdfSheetPath().isEmpty()) {
                        Button btn = createStyledLink("Spartiti (1)");
                        btn.setOnAction(e -> openFile(song.getPdfSheetPath()));
                        actionBox.getChildren().add(btn);
                    }
                    if (song.getAudioPath() != null && !song.getAudioPath().isEmpty()) {
                        Button btn = createStyledLink("Audio (1)");
                        btn.setOnAction(e -> openFile(song.getAudioPath()));
                        actionBox.getChildren().add(btn);
                    }
                    if (song.getYoutubeUrl() != null && !song.getYoutubeUrl().isEmpty()) {
                        Button btn = createStyledLink("YouTube (1)");
                        btn.setOnAction(e -> openUrl(song.getYoutubeUrl()));
                        actionBox.getChildren().add(btn);
                    }

                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }

            private Button createStyledLink(String text) {
                Button btn = new Button(text);
                btn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 5;");
                return btn;
            }
        });

        loadSongs();

        aggiungiBrano.setOnAction(event -> SceneManager.changeScene(event, "aggiungiBrano.fxml", 800, 600, true));

        searchBarSongs.textProperty().addListener((observable, oldValue, newValue) -> filterSongs(newValue));
    }

    private void loadSongs() {
        ObservableList<Song> items = FXCollections.observableArrayList(songDAO.getAllSongs());
        items.sort(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
        listaBrani.setItems(items);
    }

    private void filterSongs(String query) {
        ObservableList<Song> filteredItems = FXCollections.observableArrayList(songDAO.searchSongs(query));
        filteredItems.sort(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
        listaBrani.setItems(filteredItems);
    }

    private void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) Desktop.getDesktop().open(file);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
