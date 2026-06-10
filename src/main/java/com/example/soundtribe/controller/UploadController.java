package com.example.soundtribe.controller;

import com.example.soundtribe.entita.*;
import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.dao.ConcertDAO;
import com.example.soundtribe.dao.ExecutionDAO;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadController {
    // Navigazione
    @FXML public Button precP_Upload, nextP_Upload, backHome_Upload, Exit_Upload;

    // --- TOGGLE MODALITÀ ---
    @FXML public Button btnModeExecution, btnModeConcert;
    @FXML public VBox formEsecuzione, formConcerto;

    // --- CAMPI ESECUZIONE ---
    @FXML public TextField titoloEsecuzioneField;
    @FXML public ComboBox<Song> comboSelezionaBrano;
    @FXML public Button nuovoBrano_Upload;
    @FXML public Button tabFile, tabYoutube;
    @FXML public VBox fileUploadArea, youtubeUploadArea;
    @FXML public Button btnSelezionaFile;
    @FXML public Label lblSelectedFile;
    @FXML public TextField youtubeLinkField;
    @FXML public ComboBox<String> comboTipoFile;
    @FXML public GridPane metadataGrid;
    @FXML public CheckBox chkSelfPerformer, chkLiveRecording;

    @FXML public TextField esecutoriField;

    // --- NUOVA GESTIONE STRUMENTI MULTIPLI ---
    @FXML public MenuButton menuStrumenti;

    @FXML public TextField durataField;
    @FXML public TextField luogoField;
    @FXML public DatePicker dataRegistrazione;

    // --- CAMPI CONCERTO ---
    @FXML public TextField concertTitleField, concertArtistField, concertYoutubeField, concertLocationField;
    @FXML public DatePicker concertDateField;
    @FXML public TextArea concertDescField;

    @FXML public Button btnConfermaUpload;

    private File selectedFile;
    private boolean isYoutubeMode = false;
    private boolean isConcertMode = false;

    public void initialize(){
        setupNavigation();
        loadSongsIntoCombo();
        setupTabs();
        setupMetadataLogic();
        setupModeToggle();
        setupInstrumentMenu(); // Setup del MenuButton

        // FIX CONTRASTO: Forziamo il testo bianco all'avvio
        menuStrumenti.setStyle("-fx-text-fill: #FFFFFF; -fx-mark-color: #FFFFFF;");

        comboTipoFile.getItems().addAll("Spartito (PDF)", "Audio (MP3)", "Video (MP4)", "Altro");
        btnSelezionaFile.setOnAction(e -> pickFile());
        nuovoBrano_Upload.setOnAction(e -> SceneManager.changeScene(e, "aggiungiBrano.fxml", true));
        btnConfermaUpload.setOnAction(e -> handleUpload());
    }

    private void setupInstrumentMenu() {
        ExecutionDAO dao = new ExecutionDAO();
        List<Instrument> allInstruments = dao.getAllInstruments();

        menuStrumenti.getItems().clear();

        // 1. Aggiungi gli strumenti esistenti come CheckBox
        for (Instrument inst : allInstruments) {
            addCheckItemToMenu(inst.getName());
        }

        // 2. Aggiungi separatore
        menuStrumenti.getItems().add(new SeparatorMenuItem());

        // 3. Campo di testo per "Nuovo Strumento"
        TextField txtNew = new TextField();
        txtNew.setPromptText("Aggiungi altro...");
        txtNew.setStyle("-fx-text-fill: black;");

        CustomMenuItem customItem = new CustomMenuItem(txtNew);
        customItem.setHideOnClick(false);

        txtNew.setOnAction(e -> {
            String newVal = txtNew.getText().trim();
            if (!newVal.isEmpty()) {
                // Controlla se esiste già per evitare duplicati nella lista
                boolean exists = menuStrumenti.getItems().stream()
                        .filter(item -> item instanceof CheckMenuItem)
                        .anyMatch(item -> ((CheckMenuItem) item).getText().equalsIgnoreCase(newVal));

                if (!exists) {
                    addCheckItemToMenu(newVal);
                    updateMenuLabel();
                }
                txtNew.clear();
            }
        });

        menuStrumenti.getItems().add(customItem);
    }

    private void addCheckItemToMenu(String name) {
        CheckMenuItem item = new CheckMenuItem(name);
        item.selectedProperty().addListener((obs, oldVal, newVal) -> updateMenuLabel());

        // Inserisci prima del separatore (che è l'ultimo elemento prima del CustomMenuItem)
        int index = Math.max(0, menuStrumenti.getItems().size() - 2);
        menuStrumenti.getItems().add(index, item);
    }

    private void updateMenuLabel() {
        String selected = getSelectedInstrumentsString();
        if (selected.isEmpty()) {
            menuStrumenti.setText("Seleziona Strumenti...");
        } else {
            // Tronca il testo se troppo lungo per estetica
            if (selected.length() > 30) {
                menuStrumenti.setText(selected.substring(0, 27) + "...");
            } else {
                menuStrumenti.setText(selected);
            }
        }
        // FIX CONTRASTO: Riapplichiamo lo stile ogni volta che il testo cambia per sicurezza
        menuStrumenti.setStyle("-fx-text-fill: #FFFFFF; -fx-mark-color: #FFFFFF;");
    }

    // Helper per ottenere la stringa finale da salvare nel DB
    private String getSelectedInstrumentsString() {
        List<String> selected = new ArrayList<>();
        for (MenuItem item : menuStrumenti.getItems()) {
            if (item instanceof CheckMenuItem) {
                CheckMenuItem checkItem = (CheckMenuItem) item;
                if (checkItem.isSelected()) {
                    selected.add(checkItem.getText());
                }
            }
        }
        return String.join(", ", selected);
    }

    private void setupModeToggle() {
        btnModeExecution.setOnAction(e -> switchMode(false));
        btnModeConcert.setOnAction(e -> switchMode(true));
    }

    private void switchMode(boolean concertMode) {
        this.isConcertMode = concertMode;
        formEsecuzione.setVisible(!concertMode);
        formEsecuzione.setManaged(!concertMode);
        formConcerto.setVisible(concertMode);
        formConcerto.setManaged(concertMode);

        if (concertMode) {
            btnModeConcert.getStyleClass().add("st-segment-button-active");
            btnModeExecution.getStyleClass().remove("st-segment-button-active");
        } else {
            btnModeExecution.getStyleClass().add("st-segment-button-active");
            btnModeConcert.getStyleClass().remove("st-segment-button-active");
        }
    }

    private void handleUpload() {
        if (isConcertMode) {
            handleConcertUpload();
        } else {
            handleExecutionUpload();
        }
    }

    private void handleConcertUpload() {
        if (concertTitleField.getText().isEmpty() || concertYoutubeField.getText().isEmpty()) {
            AlertUtil.mostra("Errore", "Dati Mancanti", "Titolo e Link YouTube sono obbligatori per il concerto.", Alert.AlertType.WARNING);
            return;
        }

        int userId = UserSession.getInstance().getUserId();
        java.sql.Date sqlDate = (concertDateField.getValue() != null) ? java.sql.Date.valueOf(concertDateField.getValue()) : null;

        Concert newConcert = new Concert(
                0,
                concertTitleField.getText(),
                concertArtistField.getText(),
                concertYoutubeField.getText(),
                sqlDate,
                concertLocationField.getText(),
                concertDescField.getText(),
                userId
        );

        ConcertDAO dao = new ConcertDAO();
        dao.addConcert(newConcert);

        AlertUtil.mostra("Successo", "Concerto Caricato", "Il concerto è stato salvato correttamente.", Alert.AlertType.INFORMATION);
        resetConcertFields();
    }

    private void handleExecutionUpload() {
        if (titoloEsecuzioneField.getText() == null || titoloEsecuzioneField.getText().trim().isEmpty()) {
            AlertUtil.mostra("Errore", "Titolo Mancante", "Inserisci un titolo per questa esecuzione.", Alert.AlertType.WARNING);
            return;
        }

        if (isYoutubeMode) {
            if (youtubeLinkField.getText().isEmpty()) { AlertUtil.mostra("Errore", "Link Mancante", "Youtube Link mancante", Alert.AlertType.WARNING); return; }
        } else {
            if (selectedFile == null) { AlertUtil.mostra("Errore", "File Mancante", "File locale mancante", Alert.AlertType.WARNING); return; }
        }

        int songId = 0;
        if (comboSelezionaBrano.getValue() != null) songId = comboSelezionaBrano.getValue().getId();

        java.sql.Date sqlDate = (dataRegistrazione.getValue() != null) ? java.sql.Date.valueOf(dataRegistrazione.getValue()) : null;
        int userId = UserSession.getInstance().getUserId();

        // Recupera gli strumenti selezionati dal MenuButton
        String instrumentsString = getSelectedInstrumentsString();

        Execution newMedia = new Execution(
                0, songId, titoloEsecuzioneField.getText(),
                isYoutubeMode ? youtubeLinkField.getText() : selectedFile.getAbsolutePath(),
                comboTipoFile.getValue(), esecutoriField.getText(),
                instrumentsString, // Passiamo la stringa concatenata
                durataField.getText(), chkLiveRecording.isSelected(), sqlDate,
                luogoField.getText(), false, chkSelfPerformer.isSelected(), userId
        );

        ExecutionDAO mediaDAO = new ExecutionDAO();
        mediaDAO.addMedia(newMedia);

        AlertUtil.mostra("Successo", "Caricamento Completato", "Esecuzione caricata correttamente.", Alert.AlertType.INFORMATION);
        resetExecutionFields();
    }

    private void resetConcertFields() {
        concertTitleField.clear(); concertArtistField.clear(); concertYoutubeField.clear();
        concertLocationField.clear(); concertDescField.clear(); concertDateField.setValue(null);
    }

    private void resetExecutionFields() {
        titoloEsecuzioneField.clear(); comboSelezionaBrano.getSelectionModel().clearSelection();
        youtubeLinkField.clear(); selectedFile = null; lblSelectedFile.setText("Nessun file");
        esecutoriField.clear();

        // Reset Strumenti
        for(MenuItem item : menuStrumenti.getItems()) {
            if(item instanceof CheckMenuItem) ((CheckMenuItem) item).setSelected(false);
        }
        menuStrumenti.setText("Seleziona Strumenti...");
        // FIX CONTRASTO: Reimposta lo stile dopo il reset
        menuStrumenti.setStyle("-fx-text-fill: #FFFFFF; -fx-mark-color: #FFFFFF;");

        durataField.clear();
        chkLiveRecording.setSelected(false); chkSelfPerformer.setSelected(false);
    }

    private void setupNavigation() {
        NavigationManager.navBack(precP_Upload); NavigationManager.navForward(nextP_Upload);
        NavigationManager.updateNavigationButtons(precP_Upload, nextP_Upload);
        NavigationManager.home(backHome_Upload); NavigationManager.exit(Exit_Upload);
    }

    private void loadSongsIntoCombo() {
        SongDAO songDAO = new SongDAO();
        comboSelezionaBrano.getItems().addAll(songDAO.getAllSongs());
        comboSelezionaBrano.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (titoloEsecuzioneField.getText() == null || titoloEsecuzioneField.getText().isEmpty())) {
                titoloEsecuzioneField.setText(newVal.getTitle() + " (Cover)");
            }
        });
    }

    private void setupTabs() {
        tabFile.setOnAction(e -> {
            isYoutubeMode = false; fileUploadArea.setVisible(true); fileUploadArea.setManaged(true);
            youtubeUploadArea.setVisible(false); youtubeUploadArea.setManaged(false);
            if (!tabFile.getStyleClass().contains("st-segment-button-active")) tabFile.getStyleClass().add("st-segment-button-active");
            tabYoutube.getStyleClass().remove("st-segment-button-active");
        });
        tabYoutube.setOnAction(e -> {
            isYoutubeMode = true; fileUploadArea.setVisible(false); fileUploadArea.setManaged(false);
            youtubeUploadArea.setVisible(true); youtubeUploadArea.setManaged(true);
            if (!tabYoutube.getStyleClass().contains("st-segment-button-active")) tabYoutube.getStyleClass().add("st-segment-button-active");
            tabFile.getStyleClass().remove("st-segment-button-active");
        });
    }

    private void setupMetadataLogic() {
        chkSelfPerformer.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                int userId = UserSession.getInstance().getUserId();
                UserDAO dao = new UserDAO();
                User me = dao.getUserById(userId);
                if (me != null) { esecutoriField.setText(me.getName() + " " + me.getSurname()); esecutoriField.setDisable(true); }
            } else { esecutoriField.setDisable(false); esecutoriField.clear(); }
        });
        dataRegistrazione.setDisable(true); luogoField.setDisable(true);
        chkLiveRecording.selectedProperty().addListener((obs, oldVal, newVal) -> { dataRegistrazione.setDisable(!newVal); luogoField.setDisable(!newVal); });
        comboTipoFile.valueProperty().addListener((obs, oldVal, newVal) -> { boolean isAV = newVal != null; metadataGrid.setVisible(isAV); metadataGrid.setManaged(isAV); });
    }

    private void pickFile() {
        FileChooser fc = new FileChooser(); selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) lblSelectedFile.setText(selectedFile.getName());
    }
}