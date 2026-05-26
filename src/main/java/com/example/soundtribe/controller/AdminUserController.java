package com.example.soundtribe.controller;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.entità.Comment;
import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Map;

public class AdminUserController {

    @FXML public Button precP_AdminUser, backHome_AdminUser, Exit_AdminUser;
    @FXML public VBox commentsContainer;

    private CommentDAO commentDAO;

    @FXML
    public void initialize() {
        commentDAO = new CommentDAO();

        NavigationManager.home(backHome_AdminUser);
        NavigationManager.exit(Exit_AdminUser);
        precP_AdminUser.setOnAction(event -> SceneManager.changeScene(event, "Home.fxml", true));

        // Carica la struttura: Pubblicazioni -> Commenti associati
        loadModerationView();
    }

    private void loadModerationView() {
        commentsContainer.getChildren().clear();
        int currentUserId = UserSession.getInstance().getUserId();

        if (currentUserId <= 0) {
            Label errorLbl = new Label("Effettua il login per visualizzare questa sezione.");
            errorLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            commentsContainer.getChildren().add(errorLbl);
            return;
        }

        // Il DAO fa tutto il lavoro sporco del DB e ci restituisce i dati già raggruppati!
        Map<String, List<Comment>> groupedComments = commentDAO.getCommentsToModerate(currentUserId);

        if (groupedComments.isEmpty()) {
            Label noComments = new Label("Nessun commento ricevuto sulle tue pubblicazioni finora.");
            noComments.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            commentsContainer.getChildren().add(noComments);
            return;
        }

        // Creazione dinamica dell'interfaccia divisa per Pubblicazione
        for (Map.Entry<String, List<Comment>> entry : groupedComments.entrySet()) {
            String publicationTitle = entry.getKey();
            List<Comment> commentsList = entry.getValue();

            // 1. HEADER DELLA PUBBLICAZIONE
            VBox publicationBox = new VBox(10);
            publicationBox.setStyle("-fx-background-color: #1e1e24; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #3969da; -fx-border-width: 0 0 0 4;");

            Label titleLbl = new Label(publicationTitle);
            titleLbl.setStyle("-fx-text-fill: #3969da; -fx-font-size: 16px; -fx-font-weight: bold;");
            publicationBox.getChildren().add(titleLbl);

            // 2. LISTA DEI COMMENTI SOTTO QUELLA PUBBLICAZIONE
            for (Comment comment : commentsList) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #2b2b36; -fx-padding: 10; -fx-background-radius: 5;");

                VBox textDetails = new VBox(2);
                HBox.setHgrow(textDetails, Priority.ALWAYS);

                Label commentLbl = new Label("\"" + comment.getContent() + "\"");
                commentLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                commentLbl.setWrapText(true);

                Label infoLbl = new Label("Scritto da: " + comment.getUsername());
                infoLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

                textDetails.getChildren().addAll(commentLbl, infoLbl);

                Button deleteBtn = new Button("Rimuovi ✖");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> deleteInappropriateComment(comment.getId()));

                row.getChildren().addAll(textDetails, deleteBtn);
                publicationBox.getChildren().add(row);
            }

            commentsContainer.getChildren().add(publicationBox);
        }
    }

    private void deleteInappropriateComment(int commentId) {
        // Usiamo il metodo deleteComment già presente nel tuo CommentDAO!
        commentDAO.deleteComment(commentId);
        AlertUtil.mostra("Successo", "Commento rimosso", "Il commento inappropriato è stato eliminato con successo.", Alert.AlertType.INFORMATION);
        loadModerationView(); // Ricarica la vista
    }
}