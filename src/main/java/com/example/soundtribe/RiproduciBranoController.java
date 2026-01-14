package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.io.File;

public class RiproduciBranoController {

    @FXML public Button precP_Riproduci;
    @FXML public Button nextP_Riproduci;
    @FXML public Button backHome_Riproduci;
    @FXML public Button Exit_Riproduci;
    @FXML public Button backToListBtn;

    @FXML public Label titoloBranoLabel;
    @FXML public Label autoreBranoLabel;
    @FXML public Label genereBranoLabel;
    @FXML public Label annoBranoLabel;
    @FXML public VBox filesContainer;
    @FXML public Label noFilesLabel;

    private Song currentSong;

    @FXML
    public void initialize() {
        // Navigazione standard
        NavigationManager.updateNavigationButtons(precP_Riproduci, nextP_Riproduci);
        NavigationManager.navBack(precP_Riproduci);
        NavigationManager.navForward(nextP_Riproduci);
        NavigationManager.home(backHome_Riproduci);
        NavigationManager.exit(Exit_Riproduci);

        // Il tasto "Torna alla lista" ricarica la scena dei brani
        // Assicurati che il nome del file FXML sia corretto (braniMusicali.fxml o songs.fxml)
        backToListBtn.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true));
    }

    public void setSong(Song song) {
        this.currentSong = song;

        titoloBranoLabel.setText(song.getTitle());
        autoreBranoLabel.setText("Autore: " + song.getArtist());
        genereBranoLabel.setText(song.getGenre().toUpperCase());
        annoBranoLabel.setText("");

        loadAttachedFiles();
    }

    private void loadAttachedFiles() {
        filesContainer.getChildren().clear();
        boolean hasFiles = false;

        // Gestione PDF
        if (currentSong.getPdfSheetPath() != null && !currentSong.getPdfSheetPath().isEmpty()) {
            addFileButton("📄 Visualizza Spartito/Testo (PDF)", currentSong.getPdfSheetPath(), false);
            hasFiles = true;
        }

        // Gestione Audio
        if (currentSong.getAudioPath() != null && !currentSong.getAudioPath().isEmpty()) {
            addFileButton("🎵 Riproduci Audio", currentSong.getAudioPath(), false);
            hasFiles = true;
        }

        // Gestione YouTube
        if (currentSong.getYoutubeUrl() != null && !currentSong.getYoutubeUrl().isEmpty()) {
            addFileButton("📺 Guarda su YouTube", currentSong.getYoutubeUrl(), true);
            hasFiles = true;
        }

        if (!hasFiles) {
            noFilesLabel.setVisible(true);
            filesContainer.getChildren().add(noFilesLabel);
        }
    }

    private void addFileButton(String labelText, String pathOrUrl, boolean isUrl) {
        HBox fileRow = new HBox(15);
        fileRow.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dddddd; -fx-border-radius: 8;");
        fileRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label descLabel = new Label(labelText);
        descLabel.setFont(new Font(14));
        descLabel.setStyle("-fx-text-fill: #333333;");

        HBox spacer = new HBox();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button actionBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        actionBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

        // MODIFICA: Utilizziamo il Launcher per aprire i documenti in modo sicuro su Linux
        actionBtn.setOnAction(e -> {
            if (isUrl) {
                handleOpenUrl(pathOrUrl);
            } else {
                handleOpenFile(pathOrUrl);
            }
        });

        fileRow.getChildren().addAll(descLabel, spacer, actionBtn);
        filesContainer.getChildren().add(fileRow);
    }

    // Metodi di supporto che usano HostServices tramite il Launcher
    private void handleOpenFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                // Trasformiamo il percorso del file in un URI (es: file:/home/user/musica.mp3)
                Launcher.getInstance().openDocument(file.toURI().toString());
            } else {
                System.err.println("File non trovato: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleOpenUrl(String url) {
        try {
            // HostServices gestisce automaticamente l'apertura del browser predefinito
            Launcher.getInstance().openDocument(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}