package com.example.soundtribe.controller;

import com.example.soundtribe.*;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Execution;
import com.example.soundtribe.entità.User;
import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.manager.CommentManager;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ExploreExecutionController {

    @FXML public Button precP_Exec, nextP_Exec, backHome_Exec, Exit_Exec;
    @FXML public Label titleLabel, executorsLabel, uploaderLabel, typeLabel, dateLabel, placeLabel, instrumentsLabel;
    @FXML public VBox filesContainer;
    @FXML public Label noFilesLabel;

    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Execution currentExecution;
    private UserDAO userDAO;
    private CommentManager commentManager;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        NavigationManager.navBack(precP_Exec);
        NavigationManager.navForward(nextP_Exec);
        NavigationManager.updateNavigationButtons(precP_Exec, nextP_Exec);
        NavigationManager.home(backHome_Exec);
        NavigationManager.exit(Exit_Exec);
        precP_Exec.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", true));
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
        this.commentManager = new CommentManager(commentsContainer, newCommentArea, btnPostComment, execution.getId(), CommentManager.ResourceType.EXECUTION);
    }

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

        // Bottone SCARICA
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
