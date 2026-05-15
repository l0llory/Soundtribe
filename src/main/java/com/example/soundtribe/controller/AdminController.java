package com.example.soundtribe.controller;

import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
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
    }

    public void refreshStats(int users, int comments, int reports, String storage) {
        lblTotalUsers.setText(String.valueOf(users));
        lblTotalComments.setText(String.valueOf(comments));
        lblTotalReports.setText(String.valueOf(reports));
        lblStorageUsed.setText(storage);
    }
    
}
