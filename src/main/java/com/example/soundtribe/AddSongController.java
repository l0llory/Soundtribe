package com.example.soundtribe;

import com.example.soundtribe.dao.SongDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;

public class AddSongController {

    @FXML public Button backBtn;
    @FXML public Button homeBtn;
    @FXML public Button exitBtn;
    @FXML public Button annullaBtn;
    @FXML public Button salvaBtn;

    @FXML public Button aggiungiDocBtn;
    @FXML public Button uploadCoverBtn; // Bottone carica copertina
    @FXML public ImageView coverPreview; // Anteprima immagine

    @FXML public VBox attachmentsList; // Area dove mostriamo i file caricati
    @FXML public TextField titoloField;
    @FXML public TextField annoField;
    @FXML public TextField autoriField;
    @FXML public TextField youtubeField;
    @FXML public ComboBox<String> genereCombo;

    private String pdfPath = "";
    private String audioPath = "";
    private String coverPath = ""; // Path per il database

    private SongDAO songDAO;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();

        genereCombo.getItems().addAll(
                "Afro", "Blues", "Folk", "Indie", "Jazz", "Musica classica",
                "Pop", "Raggae", "Rap", "Reggetton", "Rock", "Trap"
        );

        // Navigazione
        backBtn.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, false));
        homeBtn.setOnAction(e -> SceneManager.changeScene(e, "Home.fxml", 800, 600, true));
        annullaBtn.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, false));
        exitBtn.setOnAction(e -> SceneManager.changeScene(e, "Autenticazione.fxml", 600, 500, true));

        // --- GESTIONE COPERTINA ---
        uploadCoverBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleziona Copertina Album");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.jpeg", "*.png"));

            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                // 1. Salva path per il DB
                coverPath = selectedFile.toURI().toString();

                // 2. Aggiorna anteprima visuale (ImageView)
                coverPreview.setImage(new Image(coverPath));
                coverPreview.setPreserveRatio(true);
                coverPreview.setFitWidth(170);
                coverPreview.setFitHeight(170);

                // 3. Aggiungi riga visiva nella lista in basso
                addVisualFileRow("🖼️ Copertina", selectedFile.getName());
            }
        });

        // --- GESTIONE DOCUMENTI (PDF/MP3) ---
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
                String fileName = selectedFile.getName();
                String typeIcon = "📁";

                // Logica di assegnazione variabili
                if (path.toLowerCase().endsWith(".pdf")) {
                    pdfPath = path;
                    typeIcon = "📄 Spartito/Testo";
                } else if (path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".wav")) {
                    audioPath = path;
                    typeIcon = "🎵 Audio";
                }

                // Aggiungi riga visiva nella lista in basso
                addVisualFileRow(typeIcon, fileName);
            }
        });

        // --- SALVATAGGIO ---
        salvaBtn.setOnAction(e -> {
            if (titoloField.getText().isEmpty() || autoriField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Dati mancanti");
                alert.setHeaderText(null);
                alert.setContentText("Per favore inserisci almeno Titolo e Autori.");
                alert.showAndWait();
                return;
            }

            // Creazione oggetto Song con TUTTI i campi incluso coverPath
            Song newSong = new Song(
                    0,
                    titoloField.getText(),
                    autoriField.getText(),
                    genereCombo.getValue(),
                    pdfPath,
                    audioPath,
                    youtubeField.getText(),
                    coverPath
            );

            songDAO.addSong(newSong);
            SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, false);
        });
    }

    // Metodo helper per creare la riga grafica del file caricato
    private void addVisualFileRow(String type, String fileName) {
        HBox fileRow = new HBox(10);
        fileRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fileRow.setPadding(new javafx.geometry.Insets(8));
        fileRow.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label typeLabel = new Label(type);
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3969da;");

        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-text-fill: #555555;");

        fileRow.getChildren().addAll(typeLabel, nameLabel);

        // Aggiunge la riga al contenitore VBox nell'FXML
        attachmentsList.getChildren().add(fileRow);
    }
}