package com.example.soundtribe;

import com.example.soundtribe.dao.EsecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.entità.Instrument; // Importa la classe Instrument
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class DizionariController {

    @FXML public Button backBtn;

    // Generi (Rimangono Stringhe)
    @FXML public TextField newGenreField;
    @FXML public Button addGenreBtn;
    @FXML public ListView<String> genreList;

    // Strumenti (Ora sono Oggetti Instrument)
    @FXML public TextField newInstrumentField;
    @FXML public Button addInstrumentBtn;
    @FXML public ListView<Instrument> instrumentList;

    // Autori (Rimangono Stringhe)
    @FXML public TextField newAuthorField;
    @FXML public Button addAuthorBtn;
    @FXML public ListView<String> authorList;

    private SongDAO songDAO;
    private EsecutionDAO esecutionDAO;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();
        esecutionDAO = new EsecutionDAO();

        backBtn.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, true));

        loadDictionaries();

        // Logica Aggiunta differenziata
        addGenreBtn.setOnAction(e -> addStringItem(genreList, newGenreField));
        addAuthorBtn.setOnAction(e -> addStringItem(authorList, newAuthorField));

        // Logica specifica per Strumenti (Oggetti)
        addInstrumentBtn.setOnAction(e -> addInstrumentItem());
    }

    private void loadDictionaries() {
        // 1. Recupera Generi e Autori (Stringhe)
        List<String> genres = songDAO.getDistinctGenres();
        List<String> authors = songDAO.getDistinctAuthors();

        // 2. Recupera Strumenti (Oggetti Instrument)
        List<Instrument> instruments = esecutionDAO.getAllInstruments();

        genreList.setItems(FXCollections.observableArrayList(genres));
        authorList.setItems(FXCollections.observableArrayList(authors));
        instrumentList.setItems(FXCollections.observableArrayList(instruments));
    }

    // Metodo generico per aggiungere Stringhe (Generi, Autori)
    private void addStringItem(ListView<String> list, TextField field) {
        String value = field.getText().trim();
        if (value.isEmpty()) return;

        // Controllo duplicati (Case Insensitive per UX migliore)
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

    // Metodo specifico per aggiungere Strumenti (Oggetti Instrument)
    private void addInstrumentItem() {
        String value = newInstrumentField.getText().trim();
        if (value.isEmpty()) return;

        Instrument newInst = new Instrument(value);

        // Il metodo .contains usa l'equals() che abbiamo definito nella classe Instrument
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
                "Il valore è stato aggiunto alla lista temporanea.\nSarà salvato permanentemente nel database quando lo userai per caricare un nuovo contenuto.",
                Alert.AlertType.INFORMATION);
    }

    private void showDuplicateAlert() {
        AlertUtil.mostra("Attenzione", "Duplicato", "Questo valore esiste già nel dizionario.", Alert.AlertType.WARNING);
    }
}