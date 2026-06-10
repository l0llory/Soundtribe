package com.example.soundtribe.controller;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.dao.SongDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Song;
import com.example.soundtribe.entita.User;
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
    @FXML public Button uploadCoverBtn;
    @FXML public ImageView coverPreview;

    @FXML public VBox attachmentsList;
    @FXML public TextField titoloField;
    @FXML public TextField annoField;
    @FXML public TextField autoriField;
    @FXML public TextField youtubeField;
    @FXML public ComboBox<String> genereCombo;

    // NUOVO CAMPO: Descrizione
    @FXML public TextArea descriptionField;

    private String pdfPath = "";
    private String audioPath = "";
    private String coverPath = "";
    private SongDAO songDAO;

    @FXML
    public void initialize() {
        songDAO = new SongDAO();

        genereCombo.getItems().addAll(
                "Afro", "Blues", "Folk", "Indie", "Jazz", "Musica classica",
                "Pop", "Raggae", "Rap", "Reggetton", "Rock", "Trap"
        );

        NavigationManager.navBack(backBtn);
        NavigationManager.home(homeBtn);
        NavigationManager.exit(exitBtn);

        uploadCoverBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.jpeg", "*.png"));
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                coverPath = selectedFile.toURI().toString();
                coverPreview.setImage(new Image(coverPath));
                coverPreview.setPreserveRatio(true);
                coverPreview.setFitWidth(170);
                coverPreview.setFitHeight(170);
                addVisualFileRow("🖼️ Copertina", selectedFile.getName());
            }
        });

        aggiungiDocBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
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
                if (path.toLowerCase().endsWith(".pdf")) {
                    pdfPath = path;
                    typeIcon = "📄 Spartito/Testo";
                } else if (path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".wav")) {
                    audioPath = path;
                    typeIcon = "🎵 Audio";
                }
                addVisualFileRow(typeIcon, fileName);
            }
        });

        salvaBtn.setOnAction(e -> {
            if (titoloField.getText().isEmpty() || autoriField.getText().isEmpty()) {
                AlertUtil.mostra("Dati mancanti", "Attenzione", "Inserisci almeno Titolo e Autori.", Alert.AlertType.WARNING);
                return;
            }

            // RECUPERO DATI UTENTE LOGGATO
            int userId = UserSession.getInstance().getUserId();
            UserDAO userDAO = new UserDAO();
            User currentUser = userDAO.getUserById(userId);

            // Valori di default se qualcosa va storto col recupero utente
            String uName = "Sconosciuto";
            String uSurname = "";
            if (currentUser != null) {
                uName = currentUser.getName();
                uSurname = currentUser.getSurname();
            }

            Song newSong = new Song(
                    0,
                    titoloField.getText(),
                    autoriField.getText(),
                    genereCombo.getValue(),
                    pdfPath,
                    audioPath,
                    youtubeField.getText(),
                    coverPath,
                    userId,         // ID Utente
                    uName,          // Nome Utente
                    uSurname,       // Cognome Utente
                    descriptionField.getText() // Descrizione
            );

            songDAO.addSong(newSong);
            SceneManager.changeScene(e, "braniMusicali.fxml", false);
        });

    }

    private void addVisualFileRow(String type, String fileName) {
        HBox fileRow = new HBox(10);
        fileRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fileRow.setPadding(new javafx.geometry.Insets(8));
        fileRow.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #dee2e6;");
        Label typeLabel = new Label(type);
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3969da;");
        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-text-fill: #555555;");
        fileRow.getChildren().addAll(typeLabel, nameLabel);
        attachmentsList.getChildren().add(fileRow);
    }

}