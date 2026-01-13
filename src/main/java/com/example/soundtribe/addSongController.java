package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;

public class addSongController {

    @FXML public Button backBtn;
    @FXML public Button exitBtn;
    @FXML public Button annullaBtn;
    @FXML public Button salvaBtn;
    @FXML public Button aggiungiDocBtn;
    @FXML public TextField titoloField;
    @FXML public TextField annoField;
    @FXML public TextField autoriField;
    @FXML public ComboBox<String> genereCombo;
    @FXML public Button homeBtn;
    @FXML public Button forwardBtn;

    private String pdfPath = "";
    private String audioPath = "";
    private SongDAO songDAO;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();

        NavigationManager.updateNavigationButtons(backBtn, forwardBtn);
        NavigationManager.navBack(backBtn);
        NavigationManager.navForward(forwardBtn);
        NavigationManager.home(homeBtn);
        NavigationManager.exit(exitBtn);

        aggiungiDocBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleziona Documenti");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Tutti i file supportati", "*.pdf", "*.mp3", "*.wav"),
                    new FileChooser.ExtensionFilter("Spartiti (PDF)", "*.pdf"),
                    new FileChooser.ExtensionFilter("Audio (MP3, WAV)", "*.mp3", "*.wav")
            );
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                String path = selectedFile.getAbsolutePath();
                if (path.toLowerCase().endsWith(".pdf")) {
                    pdfPath = path;
                } else {
                    audioPath = path;
                }
                // Feedback visivo potrebbe essere aggiunto qui
            }
        });

        salvaBtn.setOnAction(e -> {
            if (titoloField.getText().isEmpty() || autoriField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Dati mancanti");
                alert.setHeaderText(null);
                alert.setContentText("Per favore inserisci almeno Titolo e Autori.");
                alert.showAndWait();
                return;
            }

            Song newSong = new Song(
                    0,
                    titoloField.getText(),
                    autoriField.getText(),
                    genereCombo.getValue(),
                    pdfPath,
                    audioPath,
                    "" // youtubeUrl default vuoto
            );

            songDAO.addSong(newSong);
            SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, false);
        });
    }
}
