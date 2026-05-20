package com.example.soundtribe.controller;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.dao.ExecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.entità.Instrument;
import com.example.soundtribe.entità.Song;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

public class DizionariController {

    @FXML public Button backBtn;

    // 1. Titoli dei brani
    @FXML public TextField newTitleField;
    @FXML public Button addTitleBtn;
    @FXML public ListView<String> titleList;

    // 2. Generi
    @FXML public TextField newGenreField;
    @FXML public Button addGenreBtn;
    @FXML public ListView<String> genreList;

    // 3. Autori
    @FXML public TextField newAuthorField;
    @FXML public Button addAuthorBtn;
    @FXML public ListView<String> authorList;

    // 4. Strumenti (Oggetti Instrument)
    @FXML public TextField newInstrumentField;
    @FXML public Button addInstrumentBtn;
    @FXML public ListView<Instrument> instrumentList;

    private SongDAO songDAO;
    private ExecutionDAO esecutionDAO;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();
        esecutionDAO = new ExecutionDAO();

        backBtn.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", true));

        // Inizializza l'aspetto delle liste con i bottoni interattivi
        setupListCells();

        loadDictionaries();

        // Logica Aggiunta Stringhe
        addTitleBtn.setOnAction(e -> addStringItem(titleList, newTitleField));
        addGenreBtn.setOnAction(e -> addStringItem(genreList, newGenreField));
        addAuthorBtn.setOnAction(e -> addStringItem(authorList, newAuthorField));

        // Logica specifica per Strumenti (Oggetti)
        addInstrumentBtn.setOnAction(e -> addInstrumentItem());
    }

    private void loadDictionaries() {
        List<String> titles = songDAO.getDistinctTitles();
        List<String> genres = songDAO.getDistinctGenres();
        List<String> authors = songDAO.getDistinctAuthors();
        List<Instrument> instruments = esecutionDAO.getAllInstruments();

        titleList.setItems(FXCollections.observableArrayList(titles));
        genreList.setItems(FXCollections.observableArrayList(genres));
        authorList.setItems(FXCollections.observableArrayList(authors));
        instrumentList.setItems(FXCollections.observableArrayList(instruments));
    }

    // --- SETUP DELLE RIGHE PERSONALIZZATE CON I BOTTONI ---
    private void setupListCells() {

        // 1. TITOLI: Bottone "Esplora" (Invariato, funziona già correttamente)
        titleList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    HBox box = createRowContainer(item);
                    Button btn = createActionButton("Esplora");

                    btn.setOnAction(e -> {
                        List<Song> songs = songDAO.searchSongsAdvanced(item, null, null);
                        if (songs != null && !songs.isEmpty()) {
                            openExploreSong(e, songs.get(0));
                        } else {
                            AlertUtil.mostra("Attenzione", "Brano non trovato",
                                    "Questo titolo è presente nel dizionario, ma nessun utente ha ancora caricato il file del brano.",
                                    Alert.AlertType.WARNING);
                        }
                    });

                    box.getChildren().add(btn);
                    setGraphic(box);
                }
            }
        });

        // 2. AUTORI: Bottone "Cerca Brani" (Invariato, funziona già tramite stringa di testo)
        authorList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    HBox box = createRowContainer(item);
                    Button btn = createActionButton("Cerca Brani");

                    btn.setOnAction(e -> executeSearch(e, item));
                    box.getChildren().add(btn);
                    setGraphic(box);
                }
            }
        });

        // 3. STRUMENTI: Aggiunge il prefisso "instrument:" per attivare la ComboBox nella schermata di arrivo
        instrumentList.setCellFactory(param -> new ListCell<Instrument>() {
            @Override
            protected void updateItem(Instrument item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    HBox box = createRowContainer(item.toString());
                    Button btn = createActionButton("Cerca Brani");

                    btn.setOnAction(e -> executeSearch(e, "instrument:" + item.toString()));
                    box.getChildren().add(btn);
                    setGraphic(box);
                }
            }
        });

        // 4. GENERI: Aggiunge il prefisso "genre:" per attivare la ComboBox nella schermata di arrivo
        genreList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    HBox box = createRowContainer(item);
                    Button btn = createActionButton("Cerca Brani");

                    btn.setOnAction(e -> executeSearch(e, "genre:" + item));
                    box.getChildren().add(btn);
                    setGraphic(box);
                }
            }
        });
    }

    private HBox createRowContainer(String text) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(text);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(label, spacer);
        return box;
    }

    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("st-button-small");
        btn.getStyleClass().add("st-button-primary");
        return btn;
    }

    private void executeSearch(javafx.event.ActionEvent event, String query) {
        UserSession.getInstance().setLastSearchQuery(query);
        SceneManager.changeScene(event, "braniMusicali.fxml", true);
    }

    private void openExploreSong(javafx.event.ActionEvent event, Song targetSong) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/soundtribe/view/esploraBrani.fxml"));
            Parent root = loader.load();

            ExploreSongController controller = loader.getController();
            controller.setSong(targetSong);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            newScene.getStylesheets().add(SceneManager.class.getResource("/com/example/soundtribe/css/style.css").toExternalForm());

            stage.setScene(newScene);
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtil.mostra("Errore di caricamento", "Impossibile aprire il brano", "C'è stato un problema nel caricamento della pagina del brano.", Alert.AlertType.ERROR);
        }
    }

    private void addStringItem(ListView<String> list, TextField field) {
        String value = field.getText().trim();
        if (value.isEmpty()) return;

        boolean exists = list.getItems().stream().anyMatch(s -> s.equalsIgnoreCase(value));

        if (!exists) {
            list.getItems().add(value);
            list.getSelectionModel().select(value);
            list.scrollTo(value);
            field.clear();
            showSuccessAlert();
        } else {
            showDuplicateAlert();
        }
    }

    private void addInstrumentItem() {
        String value = newInstrumentField.getText().trim();
        if (value.isEmpty()) return;

        Instrument newInst = new Instrument(value);

        if (!instrumentList.getItems().contains(newInst)) {
            instrumentList.getItems().add(newInst);
            instrumentList.getSelectionModel().select(newInst);
            instrumentList.scrollTo(newInst);
            newInstrumentField.clear();
            showSuccessAlert();
        } else {
            showDuplicateAlert();
        }
    }

    private void showSuccessAlert() {
        AlertUtil.mostra("Successo", "Elemento Aggiunto",
                "Il valore è stato aggiunto al dizionario temporaneo.\nSarà salvato permanentemente nel database al primo utilizzo.",
                Alert.AlertType.INFORMATION);
    }

    private void showDuplicateAlert() {
        AlertUtil.mostra("Attenzione", "Duplicato", "Questo valore esiste già nel dizionario.", Alert.AlertType.WARNING);
    }
}