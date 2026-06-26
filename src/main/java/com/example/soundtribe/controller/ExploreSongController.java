package com.example.soundtribe.controller;

import com.example.soundtribe.*;
import com.example.soundtribe.entita.Song;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser; // Import necessario

import java.io.File;
import java.nio.file.Files;        // Import necessario per la copia
import java.nio.file.StandardCopyOption;

public class ExploreSongController {

    @FXML public Button precP_Riproduci, nextP_Riproduci, backHome_Riproduci, Exit_Riproduci;
    @FXML public Label titoloBranoLabel, autoreBranoLabel, genereBranoLabel, annoBranoLabel, uploaderLabel, descrizioneLabel;
    @FXML public VBox coverContainer;
    @FXML public Label defaultIconLabel;
    @FXML public VBox filesContainer;
    @FXML public Label noFilesLabel;

    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Song currentSong;
    private CommentManager commentManager;

    @FXML
    public void initialize() {
        NavigationManager.updateNavigationButtons(precP_Riproduci, nextP_Riproduci);
        NavigationManager.home(backHome_Riproduci);
        NavigationManager.exit(Exit_Riproduci);
        precP_Riproduci.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", true));
    }

    public void setSong(Song song) {
        this.currentSong = song;
        titoloBranoLabel.setText(song.getTitle());
        autoreBranoLabel.setText("Autore: " + song.getArtist());
        genereBranoLabel.setText(song.getGenre().toUpperCase());
        annoBranoLabel.setText("");

        if (song.getUploaderName() != null) uploaderLabel.setText("Caricato da: " + song.getUploaderName() + " " + song.getUploaderSurname());
        else uploaderLabel.setText("Caricato da: Utente sconosciuto");

        descrizioneLabel.setText((song.getDescription() != null && !song.getDescription().isEmpty()) ? song.getDescription() : "Nessuna descrizione.");

        loadCover(song);
        loadAttachedFiles();

        this.commentManager = new CommentManager(commentsContainer, newCommentArea, btnPostComment, song.getId(), CommentManager.ResourceType.SONG);
    }

    private void loadCover(Song song) {
        // Rimuovi eventuale copertina precedente
        coverContainer.getChildren().removeIf(n -> "cover".equals(n.getUserData()));

        if (song.getCoverPath() == null || song.getCoverPath().isEmpty()) {
            showDefaultIcon();
            return;
        }

        try {
            String raw = song.getCoverPath();
            // Converti percorso filesystem in URL valido per JavaFX
            String url = (raw.startsWith("http") || raw.startsWith("file:"))
                    ? raw
                    : new File(raw).toURI().toString();

            Image img = new Image(url);
            if (img.isError() || img.getWidth() == 0) {
                showDefaultIcon();
                return;
            }

            // Center-crop: ritaglia il quadrato centrale (equivalente CSS object-fit:cover)
            double w = img.getWidth(), h = img.getHeight();
            double side = Math.min(w, h);
            ImagePattern pattern = new ImagePattern(img,
                    (w - side) / 2.0 / w,
                    (h - side) / 2.0 / h,
                    side / w,
                    side / h,
                    true);

            Rectangle cover = new Rectangle(120, 120);
            cover.setArcWidth(36);
            cover.setArcHeight(36);
            cover.setFill(pattern);
            cover.setEffect(new DropShadow(14, Color.rgb(0, 0, 0, 0.65)));
            cover.setUserData("cover");

            defaultIconLabel.setVisible(false);
            defaultIconLabel.setManaged(false);
            coverContainer.getChildren().add(cover);

        } catch (Exception e) {
            showDefaultIcon();
        }
    }

    private void showDefaultIcon() {
        coverContainer.getChildren().removeIf(n -> "cover".equals(n.getUserData()));
        defaultIconLabel.setVisible(true);
        defaultIconLabel.setManaged(true);
    }

    private void loadAttachedFiles() {
        filesContainer.getChildren().clear();
        boolean hasFiles = false;

        if (currentSong.getPdfSheetPath() != null && !currentSong.getPdfSheetPath().isEmpty()) {
            addFileButton("📄 Visualizza Spartito (PDF)", currentSong.getPdfSheetPath(), false); hasFiles = true;
        }
        if (currentSong.getAudioPath() != null && !currentSong.getAudioPath().isEmpty()) {
            addFileButton("🎵 Riproduci Audio", currentSong.getAudioPath(), false); hasFiles = true;
        }
        if (currentSong.getYoutubeUrl() != null && !currentSong.getYoutubeUrl().isEmpty()) {
            addFileButton("📺 Guarda su YouTube", currentSong.getYoutubeUrl(), true); hasFiles = true;
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

        // Bottone APRI
        Button openBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        openBtn.getStyleClass().addAll("st-button-primary", "st-button-small");
        openBtn.setOnAction(e -> {
            if (isUrl) Launcher.getInstance().openDocument(pathOrUrl);
            else handleOpenFile(pathOrUrl);
        });

        fileRow.getChildren().addAll(descLabel, spacer, openBtn);

        // Bottone SCARICA (Solo per file locali, non link YouTube)
        if (!isUrl) {
            Button downloadBtn = new Button("Scarica ⬇");
            downloadBtn.getStyleClass().addAll("st-button-secondary", "st-button-small"); // Stile diverso
            downloadBtn.setOnAction(e -> handleDownloadFile(pathOrUrl));

            fileRow.getChildren().add(downloadBtn);
        }

        filesContainer.getChildren().add(fileRow);
    }

    private void handleOpenFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                // FIX per Linux/Ubuntu: Forza l'apertura col browser se è MP3/MP4 e openDocument fallisce
                String os = System.getProperty("os.name").toLowerCase();
                if ((os.contains("nix") || os.contains("nux")) && (path.endsWith(".mp3") || path.endsWith(".mp4"))) {
                    try { new ProcessBuilder("xdg-open", file.getAbsolutePath()).start(); return; } catch (Exception ignored) {}
                }
                Launcher.getInstance().openDocument(file.toURI().toString());
            } else {
                AlertUtil.mostra("Errore", "File non trovato", "Il file non esiste più.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }


    private void handleDownloadFile(String sourcePath) {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            AlertUtil.mostra("Errore", "Impossibile scaricare", "Il file sorgente non è stato trovato.", Alert.AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva file come...");
        fileChooser.setInitialFileName(sourceFile.getName());

        String ext = "";
        int i = sourcePath.lastIndexOf('.');
        if (i > 0) ext = sourcePath.substring(i+1);
        if(!ext.isEmpty()) fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File " + ext.toUpperCase(), "*." + ext));

        File destFile = fileChooser.showSaveDialog(null);

        if (destFile != null) {
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                AlertUtil.mostra("Successo", "Download completato", "File salvato in: " + destFile.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                AlertUtil.mostra("Errore", "Download fallito", "Errore durante il salvataggio: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
}
