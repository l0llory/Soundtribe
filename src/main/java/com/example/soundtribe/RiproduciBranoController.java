package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;

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

        // Il tasto "Torna alla lista" ricarica semplicemente la scena dei brani
        backToListBtn.setOnAction(event -> SceneManager.changeScene(event, "songs.fxml", 800, 600, true));
    }

    // Metodo fondamentale chiamato da SongsController per passare i dati
    public void setSong(Song song) {
        this.currentSong = song;

        // Popolamento UI
        titoloBranoLabel.setText(song.getTitle());
        autoreBranoLabel.setText("Autore: " + song.getArtist());
        genereBranoLabel.setText(song.getGenre().toUpperCase());
        // Se hai un campo anno nella classe Song usa quello, altrimenti stringa vuota
        // annoBranoLabel.setText(song.getYear() != null ? song.getYear().toString() : "");
        annoBranoLabel.setText(""); // Placeholder se non hai l'anno

        loadAttachedFiles();
    }

    private void loadAttachedFiles() {
        filesContainer.getChildren().clear();
        boolean hasFiles = false;

        // Gestione PDF (Spartiti/Testi)
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

        // Gestione visualizzazione messaggio "nessun file"
        if (!hasFiles) {
            noFilesLabel.setVisible(true);
            filesContainer.getChildren().add(noFilesLabel);
        }
    }

    // Crea dinamicamente una "Card" per ogni file
    private void addFileButton(String labelText, String pathOrUrl, boolean isUrl) {
        HBox fileRow = new HBox(15);
        fileRow.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dddddd; -fx-border-radius: 8;");
        fileRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label descLabel = new Label(labelText);
        descLabel.setFont(new Font(14));
        descLabel.setStyle("-fx-text-fill: #333333;");

        // Spaziatore per spingere il bottone a destra
        HBox spacer = new HBox();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button actionBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        actionBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        actionBtn.setOnAction(e -> {
            if (isUrl) openUrl(pathOrUrl);
            else openFile(pathOrUrl);
        });

        fileRow.getChildren().addAll(descLabel, spacer, actionBtn);
        filesContainer.getChildren().add(fileRow);
    }

    private void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) Desktop.getDesktop().open(file);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) { e.printStackTrace(); }
    }
}