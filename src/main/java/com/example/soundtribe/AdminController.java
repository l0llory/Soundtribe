package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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

    @FXML private ListView<String> moderationList;

    @FXML
    public void initialize() {
        // Inizializzazione navigazione
        updateNavigationButtons();

        precP_Admin.setOnAction(event -> {
            String prev = NavigationManager.goBack();
            if (prev != null) SceneManager.changeScene(event, prev, 800, 600, false);
        });

        nextP_Admin.setOnAction(event -> {
            String next = NavigationManager.goForward();
            if (next != null) SceneManager.changeScene(event, next, 800, 600, false);
        });

        backHome_Admin.setOnAction(event -> {
            SceneManager.changeScene(event, "Home.fxml", 800, 600, true);
        });

        Exit_Admin.setOnAction(event -> {
            SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, true);
        });

        // Caricamento dati dinamici (Esempio)
        refreshStats(156, 1247, 2, "2.3 GB");
    }

    private void updateNavigationButtons() {
        precP_Admin.setDisable(!NavigationManager.canGoBack());
        nextP_Admin.setDisable(!NavigationManager.canGoForward());
    }

    public void refreshStats(int users, int comments, int reports, String storage) {
        lblTotalUsers.setText(String.valueOf(users));
        lblTotalComments.setText(String.valueOf(comments));
        lblTotalReports.setText(String.valueOf(reports));
        lblStorageUsed.setText(storage);
    }
}
