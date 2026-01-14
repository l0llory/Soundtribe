package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #3969da; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

                    // Header: Icona + Titolo + Genere
                    HBox header = new HBox(10);
                    Label musicIcon = new Label("♫");
                    musicIcon.setTextFill(javafx.scene.paint.Color.web("#3969da"));
                    musicIcon.setFont(Font.font("System", FontWeight.BOLD, 18));

                    Label titleLabel = new Label(song.getTitle());
                    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                    titleLabel.setTextFill(javafx.scene.paint.Color.web("#3969da"));

                    Label genreTag = new Label(song.getGenre());
                    genreTag.setStyle("-fx-background-color: #f0f4ff; -fx-text-fill: #3969da; -fx-padding: 2 10; -fx-background-radius: 15; -fx-border-color: #3969da; -fx-border-radius: 15;");
                    genreTag.setFont(Font.font(10));

                    header.getChildren().addAll(musicIcon, titleLabel, genreTag);

                    // Autore
                    Label authorLabel = new Label("Autore: " + song.getArtist());
                    authorLabel.setTextFill(javafx.scene.paint.Color.web("#908888"));
                    authorLabel.setPadding(new Insets(0, 0, 0, 25));

                    // Buttons/Links Section
                    HBox actionBox = new HBox(10);
                    actionBox.setPadding(new Insets(5, 0, 0, 25));

                    // --- NUOVO BOTTONE PRINCIPALE: RIPRODUCI / VEDI DETTAGLI ---
                    Button playBtn = new Button("▶ Riproduci Brano");
                    playBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                    playBtn.setOnAction(e -> openSongDetails(song, playBtn)); // Passiamo il controllo per ottenere lo stage
                    actionBox.getChildren().add(playBtn);

                    // (Opzionale) Puoi mantenere i link rapidi o rimuoverli se vuoi tutto nell'altra pagina
                    if (song.getYoutubeUrl() != null && !song.getYoutubeUrl().isEmpty()) {
                        Button ytBtn = createStyledLink("📺 YouTube");
                        ytBtn.setOnAction(e -> openUrl(song.getYoutubeUrl()));
                        actionBox.getChildren().add(ytBtn);
                    }

                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }

            private Button createStyledLink(String text) {
                Button btn = new Button(text);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3969da; -fx-font-size: 11px; -fx-border-color: #3969da; -fx-border-radius: 5; -fx-cursor: hand;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3969da; -fx-font-size: 11px; -fx-border-color: #3969da; -fx-border-radius: 5; -fx-cursor: hand;"));
                return btn;
            }
        });

        loadSongs();

        aggiungiBrano.setOnAction(event -> SceneManager.changeScene(event, "aggiungiBrano.fxml", 800, 600, true));

        searchBarSongs.textProperty().addListener((observable, oldValue, newValue) -> filterSongs(newValue));
    }

    // Metodo per aprire la nuova scena e passare i dati
    private void openSongDetails(Song song, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("riproduciBrani.fxml"));
            Parent root = loader.load();

            // Otteniamo il controller della nuova scena e passiamo i dati del brano
            RiproduciBranoController controller = loader.getController();
            controller.setSong(song);

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) { e.printStackTrace(); }
    }
}