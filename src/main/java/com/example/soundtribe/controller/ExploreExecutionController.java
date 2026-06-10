package com.example.soundtribe.controller;

import com.example.soundtribe.*;
import com.example.soundtribe.dao.ExecutionDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Execution;
import com.example.soundtribe.entita.ExecutionSegment;
import com.example.soundtribe.entita.User;
import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.manager.CommentManager;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ExploreExecutionController {

    @FXML public Button precP_Exec, nextP_Exec, backHome_Exec, Exit_Exec;
    @FXML public Label titleLabel, executorsLabel, uploaderLabel, typeLabel, dateLabel, placeLabel, instrumentsLabel;
    @FXML public VBox filesContainer;
    @FXML public Label noFilesLabel;

    // NUOVI CAMPI PER SEGMENTI ESECUZIONE (Ricorda di aggiungerli nel FXML!)
    @FXML public VBox segmentsContainer;
    @FXML public Button btnAddSegment;

    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Execution currentExecution;
    private UserDAO userDAO;
    private ExecutionDAO executionDAO;
    private CommentManager commentManager;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        executionDAO = new ExecutionDAO();

        NavigationManager.navBack(precP_Exec);
        NavigationManager.navForward(nextP_Exec);
        NavigationManager.updateNavigationButtons(precP_Exec, nextP_Exec);
        NavigationManager.home(backHome_Exec);
        NavigationManager.exit(Exit_Exec);
        precP_Exec.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", true));

        // Azione per aprire il form di inserimento annotazione segmento
        if (btnAddSegment != null) {
            btnAddSegment.setOnAction(e -> showAddSegmentDialog());
        }
    }

    public void setExecution(Execution execution) {
        this.currentExecution = execution;
        titleLabel.setText(execution.getTitle());
        executorsLabel.setText("Esecutori: " + execution.getExecutors());
        instrumentsLabel.setText(execution.getInstruments());
        placeLabel.setText(execution.getRecordingPlace() != null ? execution.getRecordingPlace() : "-");
        dateLabel.setText(execution.getRecordingDate() != null ? execution.getRecordingDate().toString() : "-");

        if (execution.getUploaderId() > 0) {
            User u = userDAO.getUserById(execution.getUploaderId());
            uploaderLabel.setText(u != null ? "Caricato da: " + u.getName() + " " + u.getSurname() : "Caricato da: Utente " + execution.getUploaderId());
        } else uploaderLabel.setText("Caricato da: Sconosciuto");

        String typeInfo = (execution.isLive() ? "LIVE" : "STUDIO") + (execution.getSongId() == 0 ? " • INEDITO" : " • COVER");
        typeLabel.setText(typeInfo);

        loadAttachedFiles();
        loadSegments(); // Carica le note a testo libero
        this.commentManager = new CommentManager(commentsContainer, newCommentArea, btnPostComment, execution.getId(), CommentManager.ResourceType.EXECUTION);
    }

    // --- LOGICA SEGMENTI ANNOTATI E POPUP ---

    private void loadSegments() {
        if (segmentsContainer == null) return; // Evita crash se l'FXML non è stato ancora aggiornato
        segmentsContainer.getChildren().clear();

        List<ExecutionSegment> segments = executionDAO.getSegmentsForExecution(currentExecution.getId());

        if (segments.isEmpty()) {
            Label noSegments = new Label("Nessuna nota presente. Clicca su '+ Aggiungi Nota' per segnare assoli, ritmi o dettagli!");
            noSegments.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            segmentsContainer.getChildren().add(noSegments);
        } else {
            for (ExecutionSegment s : segments) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #2b2b36; -fx-padding: 8; -fx-background-radius: 5;");

                Label timeLbl = new Label("[" + s.getStartTime() + " - " + s.getEndTime() + "]");
                timeLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-min-width: 85px;");

                VBox details = new VBox(2);
                Label notesLbl = new Label(s.getNotes());
                notesLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                notesLbl.setWrapText(true);

                User u = userDAO.getUserById(s.getUserId());
                String userName = (u != null) ? (u.getName() + " " + u.getSurname()) : "Utente " + s.getUserId();
                Label userLbl = new Label("Nota aggiunta da: " + userName);
                userLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");

                details.getChildren().addAll(notesLbl, userLbl);
                row.getChildren().addAll(timeLbl, details);
                segmentsContainer.getChildren().add(row);
            }
        }
    }

    private void showAddSegmentDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Aggiungi Nota al Segmento");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e1e24;");

        Label header = new Label("Dettagli Segmento (Assoli, Strumenti, Ritmi...)");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField startField = new TextField(); startField.setPromptText("Minuto inizio (es. 01:20)");
        TextField endField = new TextField(); endField.setPromptText("Minuto fine (es. 02:15)");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Note a testo libero (es. Assolo di chitarra elettrica...)");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        grid.add(createWhiteLabel("Inizio:"), 0, 0); grid.add(startField, 1, 0);
        grid.add(createWhiteLabel("Fine:"), 0, 1); grid.add(endField, 1, 1);
        grid.add(createWhiteLabel("Note:"), 0, 2); grid.add(notesArea, 1, 2);

        Button saveBtn = new Button("Salva Nota");
        saveBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            if (startField.getText().isEmpty() || notesArea.getText().isEmpty()) return;

            int userId = UserSession.getInstance().getUserId();
            
            if (userId <= 0) {
                AlertUtil.mostra("Errore", "Non autorizzato", "Devi fare il login per aggiungere una nota.", Alert.AlertType.ERROR);
                return;
            }

            ExecutionSegment newSegment = new ExecutionSegment();
            newSegment.setExecutionId(currentExecution.getId());
            newSegment.setUserId(userId);
            newSegment.setStartTime(startField.getText());
            newSegment.setEndTime(endField.getText());
            newSegment.setNotes(notesArea.getText());

            executionDAO.addSegmentToExecution(newSegment);

            loadSegments(); // Ricarica visivamente i segmenti
            dialog.close();
        });

        layout.getChildren().addAll(header, grid, saveBtn);
        dialog.setScene(new Scene(layout, 400, 300));
        dialog.show();
    }

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white;");
        return l;
    }

    // --- LOGICA GESTIONE FILE ORIGINALE (Invariata) ---

    private void loadAttachedFiles() {
        filesContainer.getChildren().clear();
        boolean hasFiles = false;
        String path = currentExecution.getFilePath();
        if (path != null && !path.isEmpty()) {
            boolean isUrl = path.startsWith("http") || path.startsWith("www");
            String label = "File (" + (currentExecution.getFileType() != null ? currentExecution.getFileType() : "Media") + ")";
            if (isUrl) label = "Link Esterno / YouTube";
            addFileButton(label, path, isUrl);
            hasFiles = true;
        }
        if (!hasFiles) {
            noFilesLabel.setVisible(true); filesContainer.getChildren().add(noFilesLabel);
        } else {
            noFilesLabel.setVisible(false);
        }
    }

    private void addFileButton(String labelText, String pathOrUrl, boolean isUrl) {
        HBox fileRow = new HBox(15);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        fileRow.getStyleClass().add("st-card");
        fileRow.setPadding(new Insets(15));

        Label descLabel = new Label(labelText); descLabel.getStyleClass().add("st-label");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        actionBtn.getStyleClass().addAll("st-button-primary", "st-button-small");
        actionBtn.setOnAction(e -> {
            if (isUrl) Launcher.getInstance().openDocument(pathOrUrl);
            else handleOpenFile(pathOrUrl);
        });

        fileRow.getChildren().addAll(descLabel, spacer, actionBtn);

        if (!isUrl) {
            Button downloadBtn = new Button("Scarica ⬇");
            downloadBtn.getStyleClass().addAll("st-button-secondary", "st-button-small");
            downloadBtn.setOnAction(e -> handleDownloadFile(pathOrUrl));
            fileRow.getChildren().add(downloadBtn);
        }
        filesContainer.getChildren().add(fileRow);
    }

    private void handleOpenFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                String os = System.getProperty("os.name").toLowerCase();
                if ((os.contains("nix") || os.contains("nux")) && (path.endsWith(".mp3") || path.endsWith(".mp4"))) {
                    try { new ProcessBuilder("xdg-open", file.getAbsolutePath()).start(); return; } catch (Exception ignored) {}
                }
                Launcher.getInstance().openDocument(file.toURI().toString());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDownloadFile(String sourcePath) {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            AlertUtil.mostra("Errore", "Impossibile scaricare", "File non trovato.", Alert.AlertType.ERROR);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva file come...");
        fileChooser.setInitialFileName(sourceFile.getName());
        File destFile = fileChooser.showSaveDialog(null);
        if (destFile != null) {
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                AlertUtil.mostra("Successo", "Download completato", "Salvato in: " + destFile.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                AlertUtil.mostra("Errore", "Salvataggio fallito", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}