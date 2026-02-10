package com.example.soundtribe;

import com.example.soundtribe.dao.EsecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Esecution;
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

    // Sezione 1: Titolo (NUOVO)
    @FXML public TextField titoloEsecuzioneField;

    // Sezione 2: Brano
    @FXML public ComboBox<Song> comboSelezionaBrano;
    @FXML public Button nuovoBrano_Upload;

    // Sezione 3: Sorgente
    @FXML public Button tabFile;
    @FXML public Button tabYoutube;
    @FXML public VBox fileUploadArea;
    @FXML public VBox youtubeUploadArea;
    @FXML public Button btnSelezionaFile;
    @FXML public Label lblSelectedFile;
    @FXML public TextField youtubeLinkField;
    @FXML public CheckBox chkConcertoIntero;

    // Sezione 4: Metadati
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

        // Conferma Upload
        btnConfermaUpload.setOnAction(e -> handleUpload());

        // FEATURE OPZIONALE: Se seleziono un brano esistente, suggerisco un titolo se il campo è vuoto
        comboSelezionaBrano.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (titoloEsecuzioneField.getText() == null || titoloEsecuzioneField.getText().isEmpty())) {
                titoloEsecuzioneField.setText(newVal.getTitle() + " (Cover)");
            }
        });
    }

    private void setupNavigation() {
        NavigationManager.navBack(precP_Upload);
        NavigationManager.navForward(nextP_Upload);
        NavigationManager.updateNavigationButtons(precP_Upload, nextP_Upload);
        NavigationManager.home(backHome_Upload);
        NavigationManager.exit(Exit_Upload);
    }

    private void loadSongsIntoCombo() {
        SongDAO songDAO = new SongDAO();
        comboSelezionaBrano.getItems().addAll(songDAO.getAllSongs());
    }

    private void setupTabs() {
        // Toggle Logica
        tabFile.setOnAction(e -> {
            isYoutubeMode = false;
            fileUploadArea.setVisible(true);
            fileUploadArea.setManaged(true);
            youtubeUploadArea.setVisible(false);
            youtubeUploadArea.setManaged(false);

            if (!tabFile.getStyleClass().contains("st-segment-button-active")) {
                tabFile.getStyleClass().add("st-segment-button-active");
            }
            tabYoutube.getStyleClass().remove("st-segment-button-active");
        });

        tabYoutube.setOnAction(e -> {
            isYoutubeMode = true;
            fileUploadArea.setVisible(false);
            fileUploadArea.setManaged(false);
            youtubeUploadArea.setVisible(true);
            youtubeUploadArea.setManaged(true);

            if (!tabYoutube.getStyleClass().contains("st-segment-button-active")) {
                tabYoutube.getStyleClass().add("st-segment-button-active");
            }
            tabFile.getStyleClass().remove("st-segment-button-active");
        });
    }

    private void setupMetadataLogic() {
        chkSelfPerformer.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
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

        dataRegistrazione.setDisable(true);
        luogoField.setDisable(true);

        chkLiveRecording.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dataRegistrazione.setDisable(!newVal);
            luogoField.setDisable(!newVal);
        });

        comboTipoFile.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAudioVideo = newVal != null;
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
        // 1. VALIDAZIONE: Titolo obbligatorio
        if (titoloEsecuzioneField.getText() == null || titoloEsecuzioneField.getText().trim().isEmpty()) {
            AlertUtil.mostra("Errore", "Titolo Mancante", "Inserisci un titolo per questa esecuzione.", Alert.AlertType.WARNING);
            return;
        }

        // VALIDAZIONE: Sorgente
        if (isYoutubeMode) {
            if (youtubeLinkField.getText() == null || youtubeLinkField.getText().trim().isEmpty()) {
                AlertUtil.mostra("Errore", "Link Mancante", "Inserisci un link YouTube valido.", Alert.AlertType.WARNING);
                return;
            }
        } else {
            if (selectedFile == null) {
                AlertUtil.mostra("Errore", "File Mancante", "Seleziona un file locale da caricare.", Alert.AlertType.WARNING);
                return;
            }
        }

        // VALIDAZIONE: Live
        if (chkLiveRecording.isSelected()) {
            if (dataRegistrazione.getValue() == null || luogoField.getText().isEmpty()) {
                AlertUtil.mostra("Dati Mancanti", "Live Recording", "Per le registrazioni dal vivo, Data e Luogo sono obbligatori.", Alert.AlertType.ERROR);
                return;
            }
        }

        // 2. RECUPERO DATI
        int songId = 0;
        Song selectedSong = comboSelezionaBrano.getValue();
        if (selectedSong != null) {
            songId = selectedSong.getId();
        }

        java.sql.Date sqlDate = null;
        if (dataRegistrazione.getValue() != null) {
            sqlDate = java.sql.Date.valueOf(dataRegistrazione.getValue());
        }

        int currentUserId = UserSession.getInstance().getUserId();

        // 3. CREAZIONE OGGETTO ESECUTION (Aggiornato col TITOLO)
        Esecution newMedia = new Esecution(
                0, // ID auto-increment
                songId,
                titoloEsecuzioneField.getText(), // <--- TITOLO
                isYoutubeMode ? youtubeLinkField.getText() : selectedFile.getAbsolutePath(),
                comboTipoFile.getValue(),
                esecutoriField.getText(),
                strumentiField.getText(),
                durataField.getText(),
                chkLiveRecording.isSelected(),
                sqlDate,
                luogoField.getText(),
                chkConcertoIntero.isSelected(),
                chkSelfPerformer.isSelected(),
                currentUserId
        );

        // 4. SALVATAGGIO
        EsecutionDAO mediaDAO = new EsecutionDAO();
        mediaDAO.addMedia(newMedia);

        AlertUtil.mostra("Successo", "Caricamento Completato", "Il materiale è stato caricato correttamente.", Alert.AlertType.INFORMATION);

        resetFields();
    }

    private void resetFields() {
        titoloEsecuzioneField.clear(); // Resetta il titolo
        comboSelezionaBrano.getSelectionModel().clearSelection();
        youtubeLinkField.clear();
        selectedFile = null;
        lblSelectedFile.setText("Nessun file selezionato");
        esecutoriField.clear();
        strumentiField.clear();
        durataField.clear();
        chkLiveRecording.setSelected(false);
        chkConcertoIntero.setSelected(false);
        chkSelfPerformer.setSelected(false);
    }
}