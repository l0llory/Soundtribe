package com.example.soundtribe.controller;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.manager.NavigationManager;
import com.example.soundtribe.manager.SceneManager;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsersController {

    // Navigazione
    @FXML public Button precP2;
    @FXML public Button nextP2;
    @FXML public Button backHome1;
    @FXML public Button Exit2;

    // Header e Filtri
    @FXML public TextField barraRicerca;
    @FXML public ComboBox<String> tuttiRuoli;
    @FXML public ComboBox<String> tuttiStati;
    @FXML public Button invitaUtenti;

    // Liste
    @FXML public ListView<User> richiesteSospeso;
    @FXML public Label labelRichiesteSospeso;
    @FXML public ListView<User> listaUtenti;

    // Dati
    private UserDAO userDAO;
    private List<User> allUsersMasterList;
    private List<User> pendingUsersList;
    private boolean isAdmin;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        isAdmin = UserSession.getInstance().isAdmin();

        // Setup Navigazione
        NavigationManager.navBack(precP2);
        NavigationManager.navForward(nextP2);
        NavigationManager.updateNavigationButtons(precP2, nextP2);
        NavigationManager.home(backHome1);
        NavigationManager.exit(Exit2);

        // Setup Filtri
        tuttiRuoli.getItems().addAll("Tutti i ruoli", "Admin", "User");
        tuttiRuoli.getSelectionModel().selectFirst();
        tuttiStati.getItems().addAll("Tutti gli stati", "Attivo", "In Attesa");
        tuttiStati.getSelectionModel().selectFirst();

        setupUserListCellFactory();

        // GESTIONE VISIBILITÀ ADMIN
        if (isAdmin) {
            setupPendingListCellFactory();
            richiesteSospeso.setVisible(true);
            richiesteSospeso.setManaged(true);
            if(invitaUtenti != null) invitaUtenti.setVisible(true);
        } else {
            richiesteSospeso.setVisible(false);
            richiesteSospeso.setManaged(false);
            if (labelRichiesteSospeso != null) {
                labelRichiesteSospeso.setVisible(false);
                labelRichiesteSospeso.setManaged(false);
            }
            if(invitaUtenti != null) invitaUtenti.setVisible(false);
        }

        refreshData();

        // Listeners
        barraRicerca.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tuttiRuoli.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tuttiStati.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void refreshData() {
        allUsersMasterList = userDAO.getAllUsers();
        if (isAdmin) {
            pendingUsersList = userDAO.getPendingUsers();
            richiesteSospeso.setItems(FXCollections.observableArrayList(pendingUsersList));
        } else {
            richiesteSospeso.setItems(FXCollections.emptyObservableList());
        }
        applyFilters();
    }

    private void applyFilters() {
        if (allUsersMasterList == null) return;
        String query = barraRicerca.getText().toLowerCase().trim();
        String selectedRole = tuttiRuoli.getValue();

        List<User> filteredList = allUsersMasterList.stream()
                .filter(u -> {
                    boolean matchesText = query.isEmpty() ||
                            (u.getName() != null && u.getName().toLowerCase().contains(query)) ||
                            (u.getSurname() != null && u.getSurname().toLowerCase().contains(query)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(query));
                    boolean matchesRole = selectedRole == null || selectedRole.equals("Tutti i ruoli") ||
                            (u.isAdmin() && selectedRole.equals("Admin")) ||
                            (!u.isAdmin() && selectedRole.equals("User"));
                    return matchesText && matchesRole;
                })
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        listaUtenti.setItems(FXCollections.observableArrayList(filteredList));
    }

    // --- LISTA UTENTI (STANDARD) ---
    private void setupUserListCellFactory() {
        listaUtenti.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    getStyleClass().remove("st-list-cell-content");
                } else {
                    HBox card = new HBox(15);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.getStyleClass().add("st-card");

                    URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
                    Circle avatar = new Circle(20);
                    if (user.getProfilePicPath() != null && !user.getProfilePicPath().isEmpty()) {
                        try {
                            avatar.setFill(new ImagePattern(new Image(user.getProfilePicPath())));
                        } catch (Exception e) {
                            if (imageUrl != null) avatar.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
                        }
                    } else {
                        if (imageUrl != null) avatar.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
                    }

                    VBox info = new VBox(2);
                    Label nameLbl = new Label(user.getName() + " " + user.getSurname());
                    nameLbl.getStyleClass().add("st-label-blue");
                    Label emailLbl = new Label(user.getEmail());
                    emailLbl.getStyleClass().add("st-label-subtitle");
                    info.getChildren().addAll(nameLbl, emailLbl);

                    Label roleBadge = new Label(user.isAdmin() ? "ADMIN" : "USER");
                    roleBadge.getStyleClass().add(user.isAdmin() ? "st-badge-red" : "st-badge");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button actionBtn = new Button("Vedi Profilo");
                    actionBtn.getStyleClass().addAll("st-button-secondary", "st-button-small");

                    // --- SEZIONE MODIFICATA CON PASSAGGIO PARAMETRI ---
                    actionBtn.setOnAction(event -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/soundtribe/view/profiloUtente.fxml"));
                            Parent root = loader.load();

                            // Recuperiamo il controller della schermata profilo e gli passiamo l'utente cliccato
                            UserProfileViewController controller = loader.getController();
                            controller.setTargetUser(user);

                            NavigationManager.navigateTo("profiloUtente.fxml");

                            Scene currentScene = ((Node) event.getSource()).getScene();
                            Stage stage = (Stage) currentScene.getWindow();

                            double width = currentScene.getWidth();
                            double height = currentScene.getHeight();
                            boolean isMaximized = stage.isMaximized();

                            Scene newScene = new Scene(root, width, height);
                            newScene.getStylesheets().add(SceneManager.class.getResource("/com/example/soundtribe/css/style.css").toExternalForm());

                            stage.setScene(newScene);
                            if (isMaximized) {
                                stage.setMaximized(true);
                            }
                            stage.show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    card.getChildren().addAll(avatar, info, spacer, roleBadge, actionBtn);
                    setGraphic(card);
                }
            }
        });
    }

    // --- LISTA RICHIESTE SOSPESE ---
    private void setupPendingListCellFactory() {
        richiesteSospeso.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox card = new HBox(10);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(8));
                    card.getStyleClass().add("st-card");

                    VBox info = new VBox(2);
                    Label nameLbl = new Label(user.getName() + " " + user.getSurname());
                    nameLbl.getStyleClass().add("st-label");
                    Label emailLbl = new Label(user.getEmail());
                    emailLbl.getStyleClass().add("st-label-subtitle");

                    Label hint = new Label("Richiesta da valutare");
                    hint.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 10px;");

                    info.getChildren().addAll(nameLbl, emailLbl, hint);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnEvaluate = new Button("Valuta Richiesta");
                    btnEvaluate.getStyleClass().addAll("st-button-primary", "st-button-small");

                    btnEvaluate.setOnAction(e -> showEvaluationDialog(user));

                    card.getChildren().addAll(info, spacer, btnEvaluate);
                    setGraphic(card);
                }
            }
        });
    }

    // --- DIALOG DI VALUTAZIONE ---
// --- DIALOG DI VALUTAZIONE ---
    private void showEvaluationDialog(User user) {
        Stage dialog = new Stage();
        dialog.setTitle("Valutazione Richiesta");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e1e24; -fx-border-color: #333; -fx-border-width: 2;");
        layout.setPrefWidth(400);

        Label header = new Label("Valutazione Iscrizione");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(10); details.setVgap(5);
        addRow(details, 0, "Nome:", user.getName() + " " + user.getSurname());
        addRow(details, 1, "Email:", user.getEmail());

        Label motLbl = new Label("Motivazione dell'utente:");
        motLbl.setStyle("-fx-text-fill: #aaa; -fx-padding: 10 0 0 0;");

        TextArea motArea = new TextArea(user.getMotivation() != null ? user.getMotivation() : "Nessuna motivazione fornita.");
        motArea.setEditable(false);
        motArea.setWrapText(true);
        motArea.setPrefRowCount(5);
        motArea.setStyle("-fx-control-inner-background: #2b2b36; -fx-text-fill: white;");

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnReject = new Button("Rifiuta e Cancella");
        btnReject.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

        Button btnApprove = new Button("Approva Iscrizione");
        btnApprove.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");

        // --- handler per Rifiuta: chiedi motivo con TextInputDialog e poi banna
        btnReject.setOnAction(e -> {
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Motivo Rifiuto");
            reasonDialog.setHeaderText("Inserisci il motivo per cui rifiuti la richiesta di " + user.getName() + " " + user.getSurname());
            reasonDialog.setContentText("Motivo:");

            Optional<String> reasonOpt = reasonDialog.showAndWait();
            String reason = "Rifiutato durante valutazione iscrizione";
            if (reasonOpt.isPresent()) {
                String r = reasonOpt.get().trim();
                if (!r.isEmpty()) reason = r;
            }

            userDAO.updateUserStatus(user.getId(), "Banned", reason);
            refreshData();
            AlertUtil.mostra("Rifiutato", "Utente rimosso", "La richiesta è stata respinta.", Alert.AlertType.INFORMATION);
            dialog.close();
        });

        btnApprove.setOnAction(e -> {
            userDAO.updateUserStatus(user.getId(), "Verified");
            refreshData();
            AlertUtil.mostra("Approvato", "Successo", "L'utente è ora attivo.", Alert.AlertType.INFORMATION);
            dialog.close();
        });

        actions.getChildren().addAll(btnReject, btnApprove);

        layout.getChildren().addAll(header, details, motLbl, motArea, actions);
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.show();
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label); l.setStyle("-fx-text-fill: #888;");
        Label v = new Label(value); v.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        grid.add(l, 0, row); grid.add(v, 1, row);
    }
}