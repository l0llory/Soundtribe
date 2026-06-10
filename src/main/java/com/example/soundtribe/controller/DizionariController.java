package com.example.soundtribe.controller;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.dao.*;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.entita.Song;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class DizionariController {

    @FXML public Button precP_Dict;
    @FXML public Button nextP_Dict;
    @FXML public Button backHome_Dict;
    @FXML public Button Exit_Dict;
    @FXML public TabPane tabPane;

    // Sostituiamo i tipi specifici con la nostra nuova classe DictionaryRow
    @FXML public ListView<DictionaryRow> titleList;
    @FXML public ListView<DictionaryRow> genreList;
    @FXML public ListView<DictionaryRow> authorList;
    @FXML public ListView<DictionaryRow> instrumentList;

    private SongDAO songDAO;
    private ExecutionDAO esecutionDAO;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();
        esecutionDAO = new ExecutionDAO();

        // Setup Navigazione
        NavigationManager.updateNavigationButtons(precP_Dict, nextP_Dict);
        NavigationManager.home(backHome_Dict);
        NavigationManager.exit(Exit_Dict);
        setupListCells();
        loadDictionaries();
    }

    private void loadDictionaries() {
        // Carica i dati dal DAO, li raggruppa alfabeticamente e li inserisce nelle liste
        titleList.setItems(FXCollections.observableArrayList(groupAlphabetically(songDAO.getDistinctTitles())));
        genreList.setItems(FXCollections.observableArrayList(groupAlphabetically(songDAO.getDistinctGenres())));
        authorList.setItems(FXCollections.observableArrayList(groupAlphabetically(songDAO.getDistinctAuthors())));
        instrumentList.setItems(FXCollections.observableArrayList(groupAlphabetically(esecutionDAO.getAllInstruments())));
    }

    /**
     * Algoritmo che prende una lista di oggetti, la ordina in ordine alfabetico
     * e inserisce dinamicamente le lettere dell'alfabeto come separatori.
     */
    private List<DictionaryRow> groupAlphabetically(List<?> items) {
        List<DictionaryRow> result = new ArrayList<>();
        if (items == null || items.isEmpty()) return result;

        // Ordina la lista per evitare disallineamenti (Case-Insensitive)
        List<Object> sortedItems = new ArrayList<>(items);
        sortedItems.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.toString(), b.toString()));

        char currentLetter = '\0';

        for (Object item : sortedItems) {
            String text = item.toString().trim();
            if (text.isEmpty()) continue;

            // Prende la prima lettera in maiuscolo
            char firstChar = Character.toUpperCase(text.charAt(0));

            // Raggruppa i numeri o caratteri speciali sotto il simbolo '#'
            if (!Character.isLetter(firstChar)) {
                firstChar = '#';
            }

            // Se la lettera è cambiata rispetto all'elemento precedente, inserisci un Intestazione (Header)
            if (firstChar != currentLetter) {
                currentLetter = firstChar;
                result.add(new DictionaryRow(true, String.valueOf(currentLetter)));
            }

            // Inserisci l'elemento reale
            result.add(new DictionaryRow(false, text));
        }
        return result;
    }

    private void setupListCells() {
        // Utilizziamo una CellFactory personalizzata definita più in basso
        titleList.setCellFactory(param -> new DictionaryCell("Esplora", "title"));
        authorList.setCellFactory(param -> new DictionaryCell("Cerca Brani", "author"));
        instrumentList.setCellFactory(param -> new DictionaryCell("Cerca Brani", "instrument"));
        genreList.setCellFactory(param -> new DictionaryCell("Cerca Brani", "genre"));
    }

    // --- HELPER METODI ---

    private HBox createRowContainer(String text) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(label, spacer);
        return box;
    }

    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("st-button-small", "st-button-primary");
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
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(SceneManager.class.getResource("/com/example/soundtribe/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =======================================================================
    // CLASSI INTERNE PER LA GESTIONE DELLE RIGHE E DELL'ASPETTO GRAFICO
    // =======================================================================

    /**
     * Classe contenitore: Rappresenta una riga della ListView.
     * Può essere un'intestazione alfabetica (A, B, C...) o un dato reale.
     */
    public static class DictionaryRow {
        public final boolean isHeader;
        public final String text;

        public DictionaryRow(boolean isHeader, String text) {
            this.isHeader = isHeader;
            this.text = text;
        }
    }

    /**
     * Gestisce graficamente come viene disegnata ogni riga della ListView.
     */
    private class DictionaryCell extends ListCell<DictionaryRow> {
        private final String btnText;
        private final String searchType;

        public DictionaryCell(String btnText, String searchType) {
            this.btnText = btnText;
            this.searchType = searchType;
        }

        @Override
        protected void updateItem(DictionaryRow item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            }
            else if (item.isHeader) {
                // DISEGNA LA LETTERA DELL'ALFABETO (Stile Intestazione)
                Label letterLabel = new Label(item.text);
                // Colore grigio chiaro, grande e in grassetto per distinguerla dai dati
                letterLabel.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 0 5;");

                VBox box = new VBox(letterLabel);
                box.setAlignment(Pos.CENTER_LEFT);

                // Opzionale: Aggiungiamo una riga divisoria sotto la lettera
                Separator sep = new Separator();
                sep.setStyle("-fx-background-color: #555;");
                box.getChildren().add(sep);

                setGraphic(box);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                // Impedisce che la lettera sembri selezionabile dall'utente
                setMouseTransparent(true);
            }
            else {
                // DISEGNA L'ELEMENTO NORMALE CON IL BOTTONE (Testo bianco forzato)
                HBox box = createRowContainer(item.text);
                box.getStyleClass().add("st-card-clickable"); // Aggiunta per l'effetto hover
                Button btn = createActionButton(btnText);

                btn.setOnAction(e -> {
                    if (searchType.equals("title")) {
                        List<Song> songs = songDAO.searchSongsAdvanced(item.text, null, null);
                        if (!songs.isEmpty()) openExploreSong(e, songs.get(0));
                        else AlertUtil.mostra("Attenzione", "Brano non trovato", "Dati mancanti.", Alert.AlertType.WARNING);
                    } else if (searchType.equals("author")) {
                        executeSearch(e, item.text);
                    } else if (searchType.equals("genre")) {
                        executeSearch(e, "genre:" + item.text);
                    } else if (searchType.equals("instrument")) {
                        executeSearch(e, "instrument:" + item.text);
                    }
                });

                box.getChildren().add(btn);
                setGraphic(box);
                setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                setMouseTransparent(false);
            }
        }
    }
}