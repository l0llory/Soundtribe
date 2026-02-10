package com.example.soundtribe;

import com.example.soundtribe.entità.Esecution;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class ExploreExecutionController {

    @FXML public Button backBtn;
    @FXML public Label titleLabel;
    @FXML public Label executorsLabel;
    @FXML public Label instrumentsLabel;
    @FXML public Label placeLabel;
    @FXML public Label dateLabel;
    @FXML public Label typeLabel;
    @FXML public Label filePathLabel;
    @FXML public Button openFileBtn;

    private Esecution currentExecution;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, true));

        openFileBtn.setOnAction(e -> openMedia());
    }

    public void setExecution(Esecution execution) {
        this.currentExecution = execution;

        titleLabel.setText(execution.getTitle());
        executorsLabel.setText(execution.getExecutors());
        instrumentsLabel.setText(execution.getInstruments());
        placeLabel.setText(execution.getRecordingPlace() != null ? execution.getRecordingPlace() : "-");
        dateLabel.setText(execution.getRecordingDate() != null ? execution.getRecordingDate().toString() : "-");

        // Costruzione etichetta tipo (Live? Inedito?)
        String typeInfo = execution.getFileType();
        if (execution.isLive()) typeInfo += " | LIVE";
        if (execution.getSongId() == 0) typeInfo += " | INEDITO";
        else typeInfo += " | COVER";
        typeLabel.setText(typeInfo);

        filePathLabel.setText(execution.getFilePath());
    }

    private void openMedia() {
        if (currentExecution == null || currentExecution.getFilePath() == null) return;

        String path = currentExecution.getFilePath();
        try {
            if (path.startsWith("http")) {
                // Link Web (YouTube)
                Desktop.getDesktop().browse(new URI(path));
            } else {
                // File Locale
                File file = new File(path);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    System.err.println("File non trovato: " + path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}