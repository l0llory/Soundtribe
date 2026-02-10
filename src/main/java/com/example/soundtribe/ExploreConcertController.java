package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Concert;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ExploreConcertController {

    // Navigazione
    @FXML public Button precP_Concert;
    @FXML public Button nextP_Concert;
    @FXML public Button backHome_Concert;
    @FXML public Button Exit_Concert;

    // Header Info
    @FXML public Label concertTitleLabel;
    @FXML public Label concertArtistLabel;
    @FXML public Label uploaderLabel;
    @FXML public Label concertDateLabel;
    @FXML public Label concertLocationLabel;

    // Contenuti
    @FXML public TextArea descriptionArea;
    @FXML public Label linkLabel;
    @FXML public Button openLinkBtn;

    // Commenti
    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Concert currentConcert;
    private UserDAO userDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        // Setup Navigazione
        NavigationManager.navBack(precP_Concert);
        NavigationManager.navForward(nextP_Concert);
        NavigationManager.updateNavigationButtons(precP_Concert, nextP_Concert);
        NavigationManager.home(backHome_Concert);
        NavigationManager.exit(Exit_Concert);

        // Torna alla lista brani se premi indietro
        precP_Concert.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", 800, 600, true));

        openLinkBtn.setOnAction(e -> openUrl());
        btnPostComment.setOnAction(e -> postComment());
    }

    public void setConcert(Concert concert) {
        this.currentConcert = concert;

        // Popolamento UI
        concertTitleLabel.setText(concert.getTitle());
        concertArtistLabel.setText("Artista Principale: " + concert.getArtist());

        if (concert.getDate() != null) {
            concertDateLabel.setText(concert.getDate().toString());
        } else {
            concertDateLabel.setText("Data sconosciuta");
        }

        concertLocationLabel.setText(concert.getLocation() != null && !concert.getLocation().isEmpty() ? concert.getLocation() : "Luogo sconosciuto");

        // Info Uploader
        if (concert.getUploaderId() > 0) {
            User u = userDAO.getUserById(concert.getUploaderId());
            if (u != null) {
                uploaderLabel.setText("Caricato da: " + u.getName() + " " + u.getSurname());
            } else {
                uploaderLabel.setText("Caricato da: Utente " + concert.getUploaderId());
            }
        } else {
            uploaderLabel.setText("Caricato da: Admin / Sconosciuto");
        }

        // Scaletta / Descrizione
        descriptionArea.setText(concert.getDescription());

        // Link
        linkLabel.setText(concert.getYoutubeUrl());
    }

    private void openUrl() {
        if (currentConcert != null && currentConcert.getYoutubeUrl() != null) {
            try {
                Launcher.getInstance().openDocument(currentConcert.getYoutubeUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void postComment() {
        // Placeholder per la logica dei commenti sui concerti
        // Nota: Per implementare questo realmente, servirebbe aggiornare la tabella Comments
        // per accettare un concert_id oltre a song_id, oppure usare una tabella polimorfica.
        if (!newCommentArea.getText().trim().isEmpty()) {
            AlertUtil.mostra("Info", "Coming Soon", "I commenti sui concerti saranno abilitati a breve!", javafx.scene.control.Alert.AlertType.INFORMATION);
            newCommentArea.clear();
        }
    }
}