package com.example.soundtribe.controller;

import com.example.soundtribe.entità.Concert;
import com.example.soundtribe.manager.CommentManager;
import com.example.soundtribe.Launcher;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ExploreConcertController {

    @FXML public Button precP_Concert, nextP_Concert, backHome_Concert, Exit_Concert;
    @FXML public Label concertTitleLabel, concertArtistLabel, uploaderLabel, concertDateLabel, concertLocationLabel;
    @FXML public TextArea descriptionArea;
    @FXML public Label linkLabel;
    @FXML public Button openLinkBtn;

    // Comment UI Elements
    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Concert currentConcert;
    private UserDAO userDAO;
    private CommentManager commentManager;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        NavigationManager.navBack(precP_Concert);
        NavigationManager.navForward(nextP_Concert);
        NavigationManager.updateNavigationButtons(precP_Concert, nextP_Concert);
        NavigationManager.home(backHome_Concert);
        NavigationManager.exit(Exit_Concert);
        precP_Concert.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", true));
        openLinkBtn.setOnAction(e -> openUrl());
    }

    public void setConcert(Concert concert) {
        this.currentConcert = concert;

        concertTitleLabel.setText(concert.getTitle());
        concertArtistLabel.setText("Artista Principale: " + concert.getArtist());
        concertDateLabel.setText(concert.getDate() != null ? concert.getDate().toString() : "Data sconosciuta");
        concertLocationLabel.setText(concert.getLocation() != null ? concert.getLocation() : "Luogo sconosciuto");

        if (concert.getUploaderId() > 0) {
            User u = userDAO.getUserById(concert.getUploaderId());
            uploaderLabel.setText(u != null ? "Caricato da: " + u.getName() + " " + u.getSurname() : "Caricato da: Utente " + concert.getUploaderId());
        } else {
            uploaderLabel.setText("Caricato da: Admin / Sconosciuto");
        }

        descriptionArea.setText(concert.getDescription());
        linkLabel.setText(concert.getYoutubeUrl());

        // INIZIALIZZA IL COMMENT MANAGER
        this.commentManager = new CommentManager(
                commentsContainer,
                newCommentArea,
                btnPostComment,
                concert.getId(),
                CommentManager.ResourceType.CONCERT
        );
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
}
