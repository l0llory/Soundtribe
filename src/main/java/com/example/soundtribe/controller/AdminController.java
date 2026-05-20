package com.example.soundtribe.controller;

import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Comment;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class AdminController {

    @FXML private Button precP_Admin;
    @FXML private Button nextP_Admin;
    @FXML private Button backHome_Admin;
    @FXML private Button Exit_Admin;
    @FXML private Button modalitaAdmin;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalComments;
    @FXML private Label lblTotalReports;
    @FXML private Label lblStorageUsed;

    @FXML private ListView<HBox> moderationList;

    private CommentDAO commentDAO = new CommentDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        NavigationManager.navBack(precP_Admin);
        NavigationManager.navForward(nextP_Admin);
        NavigationManager.updateNavigationButtons(precP_Admin, nextP_Admin);
        NavigationManager.home(backHome_Admin);
        NavigationManager.exit(Exit_Admin);

        // Caricamento dati dinamici
        refreshStats(
                userDAO.getNumberUsers(),
                commentDAO.getTotalComments(),
                commentDAO.getCommentsByStatus("Banned"),
                "2.3 GB"
        );
        populateModerationList();
    }

    private void populateModerationList() {
        List<Comment> pendingComments = commentDAO.getPendingComments();
        ObservableList<HBox> items = FXCollections.observableArrayList();

        for (Comment comment : pendingComments) {
            HBox commentRow = createCommentRow(comment);
            items.add(commentRow);
        }

        moderationList.setItems(items);
    }

    private HBox createCommentRow(Comment comment) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 4;");
        row.setPrefHeight(80);

        // Sezione sinistra: dati del commento
        VBox commentInfo = new VBox(5);

        Label usernameLabel = new Label("👤 " + comment.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold;");

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-alignment: left;");

        commentInfo.getChildren().addAll(usernameLabel, contentLabel);
        commentInfo.setPrefWidth(450);

        // Sezione destra: bottoni
        Button banButton = new Button("🚫 Banna");
        banButton.setStyle("-fx-padding: 8px 16px; -fx-font-size: 11;");
        banButton.setOnAction(e -> updateCommentStatus(comment.getId(), "Banned"));

        Button verifyButton = new Button("✓ Verifica");
        verifyButton.setStyle("-fx-padding: 8px 16px; -fx-font-size: 11; -fx-text-fill: white; -fx-background-color: #10b981;");
        verifyButton.setOnAction(e -> updateCommentStatus(comment.getId(), "Verified"));

        HBox buttonsBox = new HBox(8);
        buttonsBox.getChildren().addAll(banButton, verifyButton);

        // Spacer per separare commenti dai bottoni
        Region spacer = new Region();
        row.getChildren().addAll(commentInfo, spacer, buttonsBox);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        return row;
    }

    public void refreshStats(int users, int comments, int reports, String storage) {
        lblTotalUsers.setText(String.valueOf(users));
        lblTotalComments.setText(String.valueOf(comments));
        lblTotalReports.setText(String.valueOf(reports));
        lblStorageUsed.setText(storage);
    }

    private void updateCommentStatus(int commentId, String newStatus) {
        commentDAO.updateCommentStatus(commentId, newStatus);
        populateModerationList(); // Ricarica la lista

        // Aggiorna anche le statistiche
        refreshStats(
                userDAO.getNumberUsers(),
                commentDAO.getTotalComments(),
                commentDAO.getCommentsByStatus("Banned"),
                "2.3 GB"
        );
    }
}