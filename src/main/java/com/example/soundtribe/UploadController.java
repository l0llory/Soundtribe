package com.example.soundtribe;

import com.example.soundtribe.dao.MediaDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.MediaFile;
import com.example.soundtribe.entità.Song;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;

public class UploadController {
    // Navigazione
    @FXML public Button precP_Upload;
    @FXML public Button nextP_Upload;
    @FXML public Button backHome_Upload;
    @FXML public Button Exit_Upload;

    // Sezione 1: Brano
    @FXML public ComboBox<Song> comboSelezionaBrano;
    @FXML public Button nuovoBrano_Upload;

    // Sezione 2: Sorgente
    @FXML public Button tabFile;
    @FXML public Button tabYoutube;
    @FXML public VBox fileUploadArea;
    @FXML public VBox youtubeUploadArea;
    @FXML public Button btnSelezionaFile;
    @FXML public Label lblSelectedFile;
    @FXML public TextField youtubeLinkField;
    @FXML public CheckBox chkConcertoIntero;

    // Sezione 3: Metadati
    @FXML public ComboBox<String> comboTipoFile;
    @FXML public GridPane metadataGrid;
    @FXML public CheckBox chkSelfPerformer;
    @FXML public TextField esecutoriField;
    @FXML public TextField strumentiField;
    @FXML public TextField durataField;
    @FXML public CheckBox chkLiveRecording;
    @FXML public DatePicker dataRegistrazione;
    @FXML public TextField luogoField;

    @FXML public Button btnConfermaUpload;

    private File selectedFile;
    private boolean isYoutubeMode = false;

    public void initialize(){
        setupNavigation();
        loadSongsIntoCombo();
        setupTabs();
        setupMetadataLogic();

        // Tipi di file supportati
        comboTipoFile.getItems().addAll("Spartito (PDF)", "Testo/Accordi", "Audio (MP3/WAV)", "Video (MP4)", "MIDI", "Altro");

        // Action selezione file
        btnSelezionaFile.setOnAction(e -> pickFile());

        // Action Nuovo Brano
        nuovoBrano_Upload.setOnAction(e -> SceneManager.changeScene(e, "aggiungiBrano.fxml", 800, 600, true));

        // Conferma Upload (Logica stub)
        btnConfermaUpload.setOnAction(e -> handleUpload());
    }

    private void setupNavigation() {
        NavigationManager.updateNavigationButtons(precP_Upload, nextP_Upload);
        NavigationManager.navBack(precP_Upload);
        NavigationManager.navForward(nextP_Upload);
        NavigationManager.home(backHome_Upload);
        NavigationManager.exit(Exit_Upload);
    }

    private void loadSongsIntoCombo() {
        SongDAO songDAO = new SongDAO();
        comboSelezionaBrano.getItems().addAll(songDAO.getAllSongs());
    }

    private void setupTabs() {
        // Logica semplice per cambiare tra File e YouTube
        tabFile.setOnAction(e -> {
            isYoutubeMode = false;
            fileUploadArea.setVisible(true);
            fileUploadArea.setManaged(true);
            youtubeUploadArea.setVisible(false);
            youtubeUploadArea.setManaged(false);

            // Stile attivo/inattivo
            tabFile.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-background-radius: 5 0 0 5;");
            tabYoutube.setStyle("-fx-background-color: white; -fx-border-color: #3969da; -fx-text-fill: #3969da; -fx-background-radius: 0 5 5 0;");
        });

        tabYoutube.setOnAction(e -> {
            isYoutubeMode = true;
            fileUploadArea.setVisible(false);
            fileUploadArea.setManaged(false);
            youtubeUploadArea.setVisible(true);
            youtubeUploadArea.setManaged(true);

            tabYoutube.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-background-radius: 0 5 5 0;");
            tabFile.setStyle("-fx-background-color: white; -fx-border-color: #3969da; -fx-text-fill: #3969da; -fx-background-radius: 5 0 0 5;");
        });
    }

    private void setupMetadataLogic() {
        // 1. Logica "Sono io l'interprete"
        chkSelfPerformer.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Se sono io, precompila con il mio nome e disabilita
                int userId = UserSession.getInstance().getUserId();
                UserDAO dao = new UserDAO();
                User me = dao.getUserById(userId);
                if (me != null) {
                    esecutoriField.setText(me.getName() + " " + me.getSurname());
                    esecutoriField.setDisable(true);
                }
            } else {
                esecutoriField.setDisable(false);
                esecutoriField.clear();
            }
        });

        // 2. Logica "Live Recording"
        // Data e Luogo sono obbligatori solo se è Live, altrimenti opzionali o disabilitati
        dataRegistrazione.setDisable(true);
        luogoField.setDisable(true);

        chkLiveRecording.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dataRegistrazione.setDisable(!newVal);
            luogoField.setDisable(!newVal);
        });

        // 3. Nascondi campi inutili se è uno spartito
        comboTipoFile.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAudioVideo = newVal != null && (newVal.contains("Audio") || newVal.contains("Video"));
            metadataGrid.setVisible(isAudioVideo);
            metadataGrid.setManaged(isAudioVideo);
        });
    }

    private void pickFile() {
        FileChooser fc = new FileChooser();
        selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            lblSelectedFile.setText(selectedFile.getName());
        }
    }

    private void handleUpload() {
        if (comboSelezionaBrano.getValue() == null) {
            AlertUtil.mostra("Errore", "Nessun Brano", "Devi selezionare un brano a cui associare il file.", Alert.AlertType.WARNING);
            return;
        }

        if (chkLiveRecording.isSelected()) {
            if (dataRegistrazione.getValue() == null || luogoField.getText().isEmpty()) {
                AlertUtil.mostra("Dati Mancanti", "Live Recording", "Per le registrazioni dal vivo, Data e Luogo sono obbligatori.", Alert.AlertType.ERROR);
                return;
            }
        }


        Song selectedSong = comboSelezionaBrano.getValue();
        int songId = selectedSong.getId();

        // Prepara la data (converti da DatePicker di JavaFX a SQL Date)
        java.sql.Date sqlDate = null;
        if (dataRegistrazione.getValue() != null) {
            sqlDate = java.sql.Date.valueOf(dataRegistrazione.getValue());
        }


        MediaFile newMedia = new MediaFile(
                0,
                songId,

                isYoutubeMode ? youtubeLinkField.getText() : (selectedFile != null ? selectedFile.getAbsolutePath() : ""),
                comboTipoFile.getValue(),
                esecutoriField.getText(),
                strumentiField.getText(),
                durataField.getText(),
                chkLiveRecording.isSelected(),
                sqlDate,
                luogoField.getText(),
                chkConcertoIntero.isSelected(),
                chkSelfPerformer.isSelected()
        );


        MediaDAO mediaDAO = new MediaDAO();
        mediaDAO.addMedia(newMedia);

        // Qui implementerai la logica di salvataggio nel DB (nuova tabella 'documents' o simile)
        AlertUtil.mostra("Successo", "Caricamento Completato", "Il materiale è stato caricato correttamente.", Alert.AlertType.INFORMATION);
    }
}