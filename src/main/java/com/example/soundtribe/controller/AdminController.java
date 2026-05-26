package com.example.soundtribe.controller;

import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Comment;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Optional;

public class AdminController {

    @FXML private Button precP_Admin;
    @FXML private Button nextP_Admin;
    @FXML private Button backHome_Admin;
    @FXML private Button Exit_Admin;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalComments;
    @FXML private Label lblTotalReports;
    @FXML private Label lblStorageUsed;

    // ToggleButton per le sezioni
    @FXML private ToggleButton tbModerazione;
    @FXML private ToggleButton tbGestioneUtenti;
    @FXML private ToggleButton tbCaricaMateriale;
    @FXML private ToggleButton tbContenutoCaricato;

    // Aree contenuto
    @FXML private VBox moderationPanel;
    @FXML private VBox gestioneUtentiPanel;
    @FXML private VBox caricaMaterialePanel;
    @FXML private VBox contenutoCaricatoPanel;

    @FXML private ListView<HBox> moderationList;
    @FXML private ListView<HBox> gestioneUtentiList;

    private CommentDAO commentDAO = new CommentDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        NavigationManager.navBack(precP_Admin);
        NavigationManager.navForward(nextP_Admin);
        NavigationManager.updateNavigationButtons(precP_Admin, nextP_Admin);
        NavigationManager.home(backHome_Admin);
        NavigationManager.exit(Exit_Admin);

        // Caricamento dati statistici
        refreshStats();

        // Setup ToggleButton listeners
        setupToggleButtonListeners();

        // Mostra Moderazione di default
        tbModerazione.setSelected(true);
        showModerationPanel();
    }


    private void setupToggleButtonListeners() {
        tbModerazione.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) showModerationPanel();
        });

        tbGestioneUtenti.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) showGestioneUtentiPanel();
        });

        tbCaricaMateriale.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) showCaricaMaterialePanel();
        });

        tbContenutoCaricato.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) showContenutoCaricatoPanel();
        });
    }

    // ========== MODERAZIONE ==========
    private void showModerationPanel() {
        moderationPanel.setVisible(true);
        moderationPanel.setManaged(true);
        gestioneUtentiPanel.setVisible(false);
        gestioneUtentiPanel.setManaged(false);
        caricaMaterialePanel.setVisible(false);
        caricaMaterialePanel.setManaged(false);
        contenutoCaricatoPanel.setVisible(false);
        contenutoCaricatoPanel.setManaged(false);
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
        row.setStyle("-fx-padding: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 4; -fx-background-color: #1e1e1e;");
        row.setPrefHeight(80);

        // Sezione sinistra: dati del commento
        VBox commentInfo = new VBox(5);

        Label usernameLabel = new Label("👤 " + comment.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #60a5fa;");

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-alignment: left; -fx-text-fill: #e5e7eb;");

        commentInfo.getChildren().addAll(usernameLabel, contentLabel);
        commentInfo.setPrefWidth(450);

        // Sezione destra: bottoni
        Button banButton = new Button("🚫 Banna");
        banButton.setStyle("-fx-padding: 8px 16px; -fx-font-size: 11; -fx-text-fill: #ef4444; -fx-background-color: transparent; -fx-border-color: #ef4444;");
        banButton.setOnAction(e -> {
            commentDAO.updateCommentStatus(comment.getId(), "Banned");
            populateModerationList();
            refreshStats();
        });

        Button verifyButton = new Button("✓ Verifica");
        verifyButton.setStyle("-fx-padding: 8px 16px; -fx-font-size: 11; -fx-text-fill: white; -fx-background-color: #10b981;");
        verifyButton.setOnAction(e -> {
            commentDAO.updateCommentStatus(comment.getId(), "Verified");
            populateModerationList();
            refreshStats();
        });

        HBox buttonsBox = new HBox(8);
        buttonsBox.getChildren().addAll(banButton, verifyButton);

        Region spacer = new Region();
        row.getChildren().addAll(commentInfo, spacer, buttonsBox);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        return row;
    }

    // ========== GESTIONE UTENTI ==========
    private void showGestioneUtentiPanel() {
        moderationPanel.setVisible(false);
        moderationPanel.setManaged(false);
        gestioneUtentiPanel.setVisible(true);
        gestioneUtentiPanel.setManaged(true);
        caricaMaterialePanel.setVisible(false);
        caricaMaterialePanel.setManaged(false);
        contenutoCaricatoPanel.setVisible(false);
        contenutoCaricatoPanel.setManaged(false);
        populateGestioneUtentiList();
    }

    private void populateGestioneUtentiList() {
        // Carica solo utenti in sospeso (non approvati)
        List<User> pendingUsers = userDAO.getPendingUsers();
        ObservableList<HBox> items = FXCollections.observableArrayList();

        if (pendingUsers.isEmpty()) {
            Label emptyLabel = new Label("Nessuna richiesta di iscrizione in sospeso");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #9ca3af;");
            HBox emptyRow = new HBox(emptyLabel);
            emptyRow.setPadding(new Insets(20));
            items.add(emptyRow);
        } else {
            for (User user : pendingUsers) {
                HBox userRow = createUserRow(user);
                items.add(userRow);
            }
        }

        gestioneUtentiList.setItems(items);
    }

    private HBox createUserRow(User user) {
        HBox row = new HBox(15);
        row.setStyle("-fx-padding: 15; -fx-border-color: #2d2d2d; -fx-border-radius: 6; -fx-border-width: 1; -fx-background-color: #1e1e1e;");
        row.setPrefHeight(180);
        row.setAlignment(Pos.CENTER_LEFT);

        // Sezione sinistra: informazioni utente
        VBox userInfo = new VBox(8);
        userInfo.setPrefWidth(350);

        Label nameLabel = new Label("👤 " + user.getName() + " " + user.getSurname());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #60a5fa;");

        Label emailLabel = new Label("📧 " + user.getEmail());
        emailLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #9ca3af;");

        Label genreLabel = new Label("🎵 Genere preferito: " + (user.getFavoriteGenre() != null ? user.getFavoriteGenre() : "Non specificato"));
        genreLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #9ca3af;");

        Label motivationLabel = new Label("📝 Motivazione: " + (user.getMotivation() != null && !user.getMotivation().isEmpty() ? user.getMotivation() : "Nessuna"));
        motivationLabel.setWrapText(true);
        motivationLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #9ca3af;");

        userInfo.getChildren().addAll(nameLabel, emailLabel, genreLabel, motivationLabel);

        // Sezione destra: bottoni Accetta e Rifiuta
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button acceptButton = new Button("✓ Accetta");
        acceptButton.setStyle("-fx-padding: 10px 20px; -fx-font-size: 12; -fx-text-fill: white; -fx-background-color: #10b981; -fx-font-weight: bold; -fx-border-radius: 6;");
        acceptButton.setPrefWidth(110);
        acceptButton.setOnAction(e -> {
            userDAO.updateUserStatus(user.getId(), true);
            System.out.println("Utente " + user.getName() + " approvato!");
            AlertUtil.mostra("Successo", "Utente Approvato",
                    "L'utente " + user.getName() + " può ora accedere all'applicazione.",
                    Alert.AlertType.INFORMATION);
            populateGestioneUtentiList(); // Ricarica la lista
            refreshStats(); // Aggiorna le statistiche
        });

        Button rejectButton = new Button("✕ Rifiuta");
        rejectButton.setStyle("-fx-padding: 10px 20px; -fx-font-size: 12; -fx-text-fill: white; -fx-background-color: #dc2626; -fx-font-weight: bold; -fx-border-radius: 6;");
        rejectButton.setPrefWidth(110);
        rejectButton.setOnAction(e -> {
            // Chiedi conferma prima di eliminare
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Conferma Rifiuto");
            confirmDialog.setHeaderText("Rifiutare iscrizione?");
            confirmDialog.setContentText("L'utente " + user.getName() + " sarà eliminato dal sistema.\nQuesta azione non può essere annullata.");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                userDAO.deleteUser(user.getId());
                AlertUtil.mostra("Completato", "Iscrizione Rifiutata",
                        "L'utente " + user.getName() + " è stato eliminato dal sistema.",
                        Alert.AlertType.INFORMATION);
                populateGestioneUtentiList(); // Ricarica la lista
                refreshStats(); // Aggiorna le statistiche
            }
        });

        buttonsBox.getChildren().addAll(acceptButton, rejectButton);

        // Spacer per separare le sezioni
        Region spacer = new Region();
        row.getChildren().addAll(userInfo, spacer, buttonsBox);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        return row;
    }

    // ========== CARICA MATERIALE ==========
    private void showCaricaMaterialePanel() {
        moderationPanel.setVisible(false);
        moderationPanel.setManaged(false);
        gestioneUtentiPanel.setVisible(false);
        gestioneUtentiPanel.setManaged(false);
        caricaMaterialePanel.setVisible(true);
        caricaMaterialePanel.setManaged(true);
        contenutoCaricatoPanel.setVisible(false);
        contenutoCaricatoPanel.setManaged(false);
        // TODO: Carica il form di upload
    }

    // ========== CONTENUTO CARICATO ==========
    private void showContenutoCaricatoPanel() {
        moderationPanel.setVisible(false);
        moderationPanel.setManaged(false);
        gestioneUtentiPanel.setVisible(false);
        gestioneUtentiPanel.setManaged(false);
        caricaMaterialePanel.setVisible(false);
        caricaMaterialePanel.setManaged(false);
        contenutoCaricatoPanel.setVisible(true);
        contenutoCaricatoPanel.setManaged(true);
        loadUserContent();
    }

    private void loadUserContent() {
        // TODO: Caricare e visualizzare il contenuto dell'admin
        System.out.println("Loading content for admin: " + UserSession.getInstance().getUserId());
    }

    // ========== UTILITY ==========
    private void refreshStats() {
        int totalUsers = userDAO.getNumberUsers(); // Utenti approvati
        int pendingUsers = userDAO.getPendingUsers().size(); // Utenti in sospeso

        lblTotalUsers.setText(String.valueOf(totalUsers) + " (" + pendingUsers + " in sospeso)");
        lblTotalComments.setText(String.valueOf(commentDAO.getTotalComments()));
        lblTotalReports.setText(String.valueOf(commentDAO.getCommentsByStatus("Banned")));
        lblStorageUsed.setText("2.3 GB");
    }
}