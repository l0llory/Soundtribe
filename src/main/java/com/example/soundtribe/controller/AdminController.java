package com.example.soundtribe.controller;

import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Comment;
import com.example.soundtribe.entità.User;
import javafx.application.Platform;
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
    @FXML private ToggleButton tbutentiSegnalati;
    @FXML private ToggleButton tbContenutoCaricato;

    @FXML private VBox moderationPanel;
    @FXML private VBox gestioneUtentiPanel;
    @FXML private VBox utentiSegnalatiPanel;
    @FXML private VBox contenutoCaricatoPanel;

    @FXML private ListView<HBox> moderationList;
    @FXML private ListView<HBox> gestioneUtentiList;
    @FXML private ListView<HBox> utentiSegnalatiList;

    // NUOVI COMPONENTI FXML per la scelta della soglia (Assicurati di mapparli nel file .fxml)
    @FXML private Spinner<Integer> spinnerSoglia;
    @FXML private Button btnFiltraSoglia;

    private CommentDAO commentDAO = new CommentDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        NavigationManager.navBack(precP_Admin);
        NavigationManager.navForward(nextP_Admin);
        NavigationManager.updateNavigationButtons(precP_Admin, nextP_Admin);
        NavigationManager.home(backHome_Admin);
        NavigationManager.exit(Exit_Admin);

        // Inizializza lo Spinner per la soglia (es. da 1 a 100, valore iniziale 3)
        if (spinnerSoglia != null) {
            spinnerSoglia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 3));
            // Listener opzionale: aggiorna la lista appena cambia il valore dello spinner
            spinnerSoglia.valueProperty().addListener((obs, oldValue, newValue) -> {
                populateUtentiSegnalatiList(newValue);
            });
        }

        // Se usi un bottone invece del listener automatico dello spinner:
        if (btnFiltraSoglia != null) {
            btnFiltraSoglia.setOnAction(e -> {
                int soglia = spinnerSoglia.getValue();
                populateUtentiSegnalatiList(soglia);
            });
        }

        refreshStats();
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

        tbutentiSegnalati.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) showUtentiSegnalatiPanel();
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
        utentiSegnalatiPanel.setVisible(false);
        utentiSegnalatiPanel.setManaged(false);
        contenutoCaricatoPanel.setVisible(false);
        contenutoCaricatoPanel.setManaged(false);
        populateModerationList();
    }

    private void populateModerationList() {
        List<Comment> ReportedComments = commentDAO.getCommentsListByStatus("Reported");
        ObservableList<HBox> items = FXCollections.observableArrayList();

        for (Comment comment : ReportedComments) {
            HBox commentRow = createCommentRow(comment);
            items.add(commentRow);
        }
        moderationList.setItems(items);
    }

    private HBox createCommentRow(Comment comment) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 4; -fx-background-color: #1e1e1e;");
        row.setPrefHeight(80);

        VBox commentInfo = new VBox(5);
        Label usernameLabel = new Label("👤 " + comment.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #60a5fa;");

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-alignment: left; -fx-text-fill: #e5e7eb;");

        commentInfo.getChildren().addAll(usernameLabel, contentLabel);
        commentInfo.setPrefWidth(450);

        Button banButton = new Button("🗑️ Banna definitivo");
        banButton.setStyle("-fx-padding: 8px 16px; -fx-font-size: 11; -fx-text-fill: #ef4444; -fx-background-color: transparent; -fx-border-color: #ef4444;");
        banButton.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Conferma Ban");
            confirmDialog.setHeaderText("Bannare definitivamente il commento?");
            confirmDialog.setContentText("Il commento è stato bannato.");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                commentDAO.updateCommentStatus(comment.getId(), "Banned");
                populateModerationList();
                refreshStats();
            }
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
        utentiSegnalatiPanel.setVisible(false);
        utentiSegnalatiPanel.setManaged(false);
        contenutoCaricatoPanel.setVisible(false);
        contenutoCaricatoPanel.setManaged(false);
        populateGestioneUtentiList();
    }

    private void populateGestioneUtentiList() {
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

        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button acceptButton = new Button("✓ Accetta");
        acceptButton.setStyle("-fx-padding: 10px 20px; -fx-font-size: 12; -fx-text-fill: white; -fx-background-color: #10b981; -fx-font-weight: bold; -fx-border-radius: 6;");
        acceptButton.setPrefWidth(110);
        acceptButton.setOnAction(e -> {
            userDAO.updateUserStatus(user.getId(), "Verified");
            AlertUtil.mostra("Successo", "Utente Approvato", "L'utente " + user.getName() + " può ora accedere.", Alert.AlertType.INFORMATION);
            populateGestioneUtentiList();
            refreshStats();
        });

        Button rejectButton = new Button("✕ Rifiuta");
        rejectButton.setStyle("-fx-padding: 10px 20px; -fx-font-size: 12; -fx-text-fill: white; -fx-background-color: #dc2626; -fx-font-weight: bold; -fx-border-radius: 6;");
        rejectButton.setPrefWidth(110);
        rejectButton.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Conferma Rifiuto");
            confirmDialog.setHeaderText("Rifiutare iscrizione?");
            confirmDialog.setContentText("L'utente " + user.getName() + " sarà eliminato dal sistema.");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Modificato: ora aggiorna lo stato in "Banned" invece di eliminare il record
                userDAO.updateUserStatus(user.getId(), "Banned");

                // Aggiornato il testo dell'alert di conferma
                AlertUtil.mostra("Completato", "Utente Bannato", "L'utente è stato contrassegnato come Bannato.", Alert.AlertType.INFORMATION);

                populateGestioneUtentiList();
                refreshStats();
            }
        });

        buttonsBox.getChildren().addAll(acceptButton, rejectButton);

        Region spacer = new Region();
        row.getChildren().addAll(userInfo, spacer, buttonsBox);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        return row;
    }

    // ========== UTENTI SEGNALATI (CORRETTO E IMPLEMENTATO) ==========
    private void showUtentiSegnalatiPanel() {
        moderationPanel.setVisible(false);
        moderationPanel.setManaged(false);
        gestioneUtentiPanel.setVisible(false);
        gestioneUtentiPanel.setManaged(false);
        utentiSegnalatiPanel.setVisible(true);
        utentiSegnalatiPanel.setManaged(true);
        contenutoCaricatoPanel.setVisible(false);
        contenutoCaricatoPanel.setManaged(false);

        // Legge il valore iniziale dello spinner e popola la lista
        int sogliaIniziale = (spinnerSoglia != null) ? spinnerSoglia.getValue() : 3;
        populateUtentiSegnalatiList(sogliaIniziale);
    }

    private void populateUtentiSegnalatiList(int soglia) {
        // Assume che userDAO abbia un metodo simile o che filtri per commenti bannati
        List<User> reportedUsers = userDAO.getUsersWithReportsAbove(soglia);
        ObservableList<HBox> items = FXCollections.observableArrayList();

        if (reportedUsers.isEmpty()) {
            Label emptyLabel = new Label("Nessun utente ha superato la soglia di " + soglia + " commenti bannati.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #9ca3af;");
            HBox emptyRow = new HBox(emptyLabel);
            emptyRow.setPadding(new Insets(20));
            items.add(emptyRow);
        } else {
            for (User user : reportedUsers) {
                HBox userRow = createReportedUserRow(user);
                items.add(userRow);
            }
        }
        utentiSegnalatiList.setItems(items);
    }

    // Metodo implementato ex-novo per creare la riga dell'utente segnalato
    private HBox createReportedUserRow(User user) {
        HBox row = new HBox(15);
        row.setStyle("-fx-padding: 15; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-color: #1e1e1e;");
        row.setPrefHeight(100);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox userInfo = new VBox(5);
        Label nameLabel = new Label("⚠️ " + user.getName() + " " + user.getSurname());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #ef4444;");

        Label emailLabel = new Label("📧 Email: " + user.getEmail());
        emailLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #9ca3af;");

        // Nota: Assicurati che l'oggetto User gestisca un contatore (es. getBannedCommentsCount())
        // In alternativa, puoi fare una query sul DB. Qui ipotizziamo un getter nell'entità.
        Label countLabel = new Label("🚫 Commenti Bannati totali: " + userDAO.getBannedCommentsCount());;
        countLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        userInfo.getChildren().addAll(nameLabel, emailLabel, countLabel);

        // Bottone Azione: es. Sospendi/Elimina Utente Malizioso
        Button suspendButton = new Button("Sospendi Account");
        suspendButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-border-radius: 4;");
        suspendButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Sospensione Utente");
            confirm.setHeaderText("Vuoi sospendere l'utente " + user.getName() + "?");

            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                userDAO.updateUserStatus(user.getId(), "Banned");// o un metodo dedicato di ban/sospensione
                AlertUtil.mostra("Successo", "Utente Sospeso", "L'utente è stato bannato.", Alert.AlertType.INFORMATION);
                populateUtentiSegnalatiList(spinnerSoglia.getValue());
                refreshStats();
            }
        });

        Region spacer = new Region();
        row.getChildren().addAll(userInfo, spacer, suspendButton);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        return row;
    }

    // ========== CONTENUTO CARICATO ==========
    private void showContenutoCaricatoPanel() {
        moderationPanel.setVisible(false);
        moderationPanel.setManaged(false);
        gestioneUtentiPanel.setVisible(false);
        gestioneUtentiPanel.setManaged(false);
        utentiSegnalatiPanel.setVisible(false);
        utentiSegnalatiPanel.setManaged(false);
        contenutoCaricatoPanel.setVisible(true);
        contenutoCaricatoPanel.setManaged(true);
        loadUserContent();
    }

    private void loadUserContent() {
        System.out.println("Loading content for admin: " + UserSession.getInstance().getUserId());
    }

    // ========== UTILITY ==========
    private void refreshStats() {
        int totalUsers = userDAO.getNumberUsersByStatus("Verified");
        int pendingUsers = userDAO.getNumberUsersByStatus("Pending");

        lblTotalUsers.setText(String.valueOf(totalUsers) + " (" + pendingUsers + " in sospeso)");
        lblTotalComments.setText(String.valueOf(commentDAO.getTotalComments()));
        lblTotalReports.setText(String.valueOf(commentDAO.getCommentsByStatus("Banned")));
        lblStorageUsed.setText("2.3 GB");
    }
}