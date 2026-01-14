package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
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
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
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
                    // Contenitore principale della Card
                    VBox card = new VBox(10);
                    card.setPadding(new Insets(15));
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #3969da; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

                    // --- HEADER: Immagine/Icona + Titolo + Genere ---
                    HBox header = new HBox(15); // Aumentato spazio tra icona e testo
                    header.setAlignment(Pos.CENTER_LEFT);

                    // 1. Gestione Icona o Immagine di Copertina
                    Node visualElement;

                    // SE L'UTENTE CHE HA CARICO IL BRANO MUSICALE HA DECISO DI CARICARE ANCHE UNA IMMAGINE DI COPERTINA
                    // ALLORA NELLA LISTVIEW APPARIRÀ L'IMMAGINE CARICATA, SENNÒ APPARIRÀ LA COPERTINA STANDARD DEI BRANI MUSICALI

                    if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
                        // Tenta di caricare l'immagine
                        try {
                            ImageView coverImg = new ImageView(new Image(song.getCoverPath()));
                            coverImg.setFitWidth(50);
                            coverImg.setFitHeight(50);
                            coverImg.setPreserveRatio(true); // O false se vuoi forzare il quadrato

                            // Arrotondiamo gli angoli dell'immagine per stile
                            Rectangle clip = new Rectangle(50, 50);
                            clip.setArcWidth(10);
                            clip.setArcHeight(10);
                            coverImg.setClip(clip);

                            visualElement = coverImg;
                        } catch (Exception e) {
                            // Se l'immagine non si carica (file spostato/perso), torna all'icona
                            visualElement = createDefaultIcon();
                        }
                    } else {
                        // Nessuna copertina: usa icona standard
                        visualElement = createDefaultIcon();
                    }

                    // 2. Info Testuali (Titolo e Genere)
                    Label titleLabel = new Label(song.getTitle());
                    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                    titleLabel.setTextFill(javafx.scene.paint.Color.web("#3969da"));

                    Label genreTag = new Label(song.getGenre());
                    genreTag.setStyle("-fx-background-color: #f0f4ff; -fx-text-fill: #3969da; -fx-padding: 2 10; -fx-background-radius: 15; -fx-border-color: #3969da; -fx-border-radius: 15;");
                    genreTag.setFont(Font.font(10));

                    // Raggruppo Titolo e Genere per allinearli meglio vicino alla foto
                    HBox titleGroup = new HBox(10, titleLabel, genreTag);
                    titleGroup.setAlignment(Pos.CENTER_LEFT);

                    header.getChildren().addAll(visualElement, titleGroup);

                    // --- DETTAGLI AUTORE ---
                    Label authorLabel = new Label("Autore: " + song.getArtist());
                    authorLabel.setTextFill(javafx.scene.paint.Color.web("#908888"));
                    // Aggiustiamo il padding per allinearlo visivamente al testo sopra (50px img + 15px gap)
                    authorLabel.setPadding(new Insets(0, 0, 0, 65));

                    // --- BOTTONI ---
                    HBox actionBox = new HBox(10);
                    actionBox.setPadding(new Insets(5, 0, 0, 65));

                    Button playBtn = new Button("▶ Riproduci Brano");
                    playBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                    playBtn.setOnAction(e -> openSongDetails(song, playBtn));
                    actionBox.getChildren().add(playBtn);

                    if (song.getYoutubeUrl() != null && !song.getYoutubeUrl().isEmpty()) {
                        Button ytBtn = createStyledLink("📺 YouTube");
                        // FIX PER LINUX: Usiamo Launcher invece di Desktop
                        ytBtn.setOnAction(e -> openUrlSafe(song.getYoutubeUrl()));
                        actionBox.getChildren().add(ytBtn);
                    }

                    card.getChildren().addAll(header, authorLabel, actionBox);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }

            // Metodo helper per creare l'icona musicale standard
            private Label createDefaultIcon() {
                Label musicIcon = new Label("♫");
                musicIcon.setTextFill(javafx.scene.paint.Color.web("#3969da"));
                musicIcon.setFont(Font.font("System", FontWeight.BOLD, 24)); // Un po' più grande
                musicIcon.setMinWidth(50); // Mantiene lo spazio riservato come se fosse una foto
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

        loadSongs();

        aggiungiBrano.setOnAction(event -> SceneManager.changeScene(event, "aggiungiBrano.fxml", 800, 600, true));
        searchBarSongs.textProperty().addListener((observable, oldValue, newValue) -> filterSongs(newValue));
    }

    private void openSongDetails(Song song, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("riproduciBrani.fxml"));
            Parent root = loader.load();
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

    // Metodo sicuro per aprire URL su Linux
    private void openUrlSafe(String url) {
        try {
            Launcher.getInstance().openDocument(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}