package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


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
        // Inizializziamo il DAO che gestirà tutto l'SQL
        songDAO = new SongDAO();

        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.navBack(precP_Songs);
        NavigationManager.navForward(nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

        // Caricamento iniziale dei dati
        loadSongs();

        // Gestione della ricerca dinamica
        searchBarSongs.textProperty().addListener((observable, oldValue, newValue) -> filterSongs(newValue));
    }

    private void loadSongs() {
        ObservableList<Song> items = FXCollections.observableArrayList(songDAO.getAllSongs());
        listaBrani.setItems(items);
    }

    private void filterSongs(String query) {
        ObservableList<Song> filteredItems = FXCollections.observableArrayList(songDAO.searchSongs(query));
        listaBrani.setItems(filteredItems);
    }
}
