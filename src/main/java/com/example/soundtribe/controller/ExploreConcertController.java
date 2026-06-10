package com.example.soundtribe.controller;

import com.example.soundtribe.entita.Concert;
import com.example.soundtribe.entita.ConcertTrack;
import com.example.soundtribe.dao.*;
import com.example.soundtribe.manager.CommentManager;
import com.example.soundtribe.Launcher;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.entita.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ExploreConcertController {

    @FXML public Button precP_Concert, nextP_Concert, backHome_Concert, Exit_Concert;
    @FXML public Label concertTitleLabel, concertArtistLabel, uploaderLabel, concertDateLabel, concertLocationLabel;

    // NUOVI CAMPI SCALETTA
    @FXML public Label descriptionLabel;
    @FXML public VBox tracksContainer;
    @FXML public Button btnAddTrack;

    @FXML public Label linkLabel;
    @FXML public Button openLinkBtn;

    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Concert currentConcert;
    private UserDAO userDAO;
    private ConcertDAO concertDAO;
    private CommentManager commentManager;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        concertDAO = new ConcertDAO();

        NavigationManager.navBack(precP_Concert);
        NavigationManager.navForward(nextP_Concert);
        NavigationManager.updateNavigationButtons(precP_Concert, nextP_Concert);
        NavigationManager.home(backHome_Concert);
        NavigationManager.exit(Exit_Concert);
        precP_Concert.setOnAction(e -> SceneManager.changeScene(e, "braniMusicali.fxml", true));
        openLinkBtn.setOnAction(e -> openUrl());

        // Azione per aprire il form di inserimento brano
        btnAddTrack.setOnAction(e -> showAddTrackDialog());
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
        } else uploaderLabel.setText("Caricato da: Admin / Sconosciuto");

        descriptionLabel.setText(concert.getDescription());
        linkLabel.setText(concert.getYoutubeUrl());

        // Carica la scaletta
        loadTracks();

        this.commentManager = new CommentManager(commentsContainer, newCommentArea, btnPostComment, concert.getId(), CommentManager.ResourceType.CONCERT);
    }

    // --- LOGICA SCALETTA E POPUP ---

    private void loadTracks() {
        tracksContainer.getChildren().clear();
        List<ConcertTrack> tracks = concertDAO.getTracksForConcert(currentConcert.getId());

        if (tracks.isEmpty()) {
            Label noTracks = new Label("Nessun brano aggiunto alla scaletta. Clicca su '+ Aggiungi Brano' per iniziare!");
            noTracks.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            tracksContainer.getChildren().add(noTracks);
        } else {
            for (ConcertTrack t : tracks) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #2b2b36; -fx-padding: 8; -fx-background-radius: 5;");

                Label timeLbl = new Label("[" + t.getStartTime() + " - " + t.getEndTime() + "]");
                timeLbl.setStyle("-fx-text-fill: #3969da; -fx-font-weight: bold;");

                VBox details = new VBox(2);
                Label titleLbl = new Label(t.getTitle());
                titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                String subtitle = "Di: " + t.getExecutors();
                if (t.getInstruments() != null && !t.getInstruments().isEmpty()) {
                    subtitle += " | Strumenti: " + t.getInstruments();
                }
                Label subLbl = new Label(subtitle);
                subLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");

                details.getChildren().addAll(titleLbl, subLbl);
                row.getChildren().addAll(timeLbl, details);
                tracksContainer.getChildren().add(row);
            }
        }
    }

    private void showAddTrackDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Aggiungi Brano alla Scaletta");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e1e24;");

        Label header = new Label("Dettagli Brano Eseguito");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField titleField = new TextField(); titleField.setPromptText("Titolo brano");
        TextField execField = new TextField(); execField.setPromptText("Esecutori (es. Band intera)");
        TextField startField = new TextField(); startField.setPromptText("Minuto inizio (es. 12:30)");
        TextField endField = new TextField(); endField.setPromptText("Minuto fine (es. 16:45)");
        TextField instField = new TextField(); instField.setPromptText("Strumenti (es. Chitarra, Basso)");

        grid.add(createWhiteLabel("Titolo:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(createWhiteLabel("Esecutori:"), 0, 1); grid.add(execField, 1, 1);
        grid.add(createWhiteLabel("Inizio:"), 0, 2); grid.add(startField, 1, 2);
        grid.add(createWhiteLabel("Fine:"), 0, 3); grid.add(endField, 1, 3);
        grid.add(createWhiteLabel("Strumenti:"), 0, 4); grid.add(instField, 1, 4);

        Button saveBtn = new Button("Salva Brano");
        saveBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            if (titleField.getText().isEmpty() || startField.getText().isEmpty()) return;

            ConcertTrack newTrack = new ConcertTrack(
                    titleField.getText(), execField.getText(),
                    startField.getText(), endField.getText(), instField.getText()
            );

            // Salva nel database (e aggiorna dizionari/relazioni)
            concertDAO.addTrackToConcert(currentConcert.getId(), newTrack);

            // Ricarica visivamente
            loadTracks();
            dialog.close();
        });

        layout.getChildren().addAll(header, grid, saveBtn);
        dialog.setScene(new Scene(layout, 400, 350));
        dialog.show();
    }

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white;");
        return l;
    }

    // --- ALTRI METODI ---

    private void openUrl() {
        if (currentConcert != null && currentConcert.getYoutubeUrl() != null) {
            try { Launcher.getInstance().openDocument(currentConcert.getYoutubeUrl()); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }
}