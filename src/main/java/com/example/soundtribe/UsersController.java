package com.example.soundtribe;

import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
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

        // 1. Setup Navigazione
        NavigationManager.navBack(precP2);
        NavigationManager.navForward(nextP2);
        NavigationManager.updateNavigationButtons(precP2, nextP2);
        NavigationManager.home(backHome1);
        NavigationManager.exit(Exit2);

        // 2. Setup Filtri
        tuttiRuoli.getItems().addAll("Tutti i ruoli", "Admin", "User");
        tuttiRuoli.getSelectionModel().selectFirst();
        tuttiStati.getItems().addAll("Tutti gli stati", "Attivo", "In Attesa");
        tuttiStati.getSelectionModel().selectFirst();

        // 3. Configurazione Grafica
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

        // 4. Caricamento Dati
        refreshData();

        // 5. Listeners
        barraRicerca.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tuttiRuoli.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tuttiStati.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        String pendingSearch = UserSession.getInstance().getLastSearchQuery();
        if (pendingSearch != null && !pendingSearch.isEmpty()) {
            barraRicerca.setText(pendingSearch);
            UserSession.getInstance().setLastSearchQuery(null);
        }

        if(invitaUtenti != null) {
            invitaUtenti.setOnAction(e -> AlertUtil.mostra("Invita Utente", "Funzionalità in arrivo", "Presto potrai invitare amici via email.", Alert.AlertType.INFORMATION));
        }
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

    // --- LISTA UTENTI PRINCIPALE ---
    private void setupUserListCellFactory() {
        listaUtenti.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    // Rimuovi eventuali classi residue se la cella viene riciclata
                    getStyleClass().remove("st-list-cell-content");
                } else {
                    // Contenitore Card
                    HBox card = new HBox(15);
                    card.setAlignment(Pos.CENTER_LEFT);
                    // Usa classe CSS per la card (sfondo, bordi, padding gestiti qui)
                    card.getStyleClass().add("st-card");
                    // Nota: Se st-card ha troppo padding per una lista, puoi creare una classe specifica .st-list-card nel CSS

                    // Avatar
                    URL imageUrl = getClass().getResource("/com/example/soundtribe/img/user.png");
                    Circle avatar = new Circle(20);
                    // Gestione immagine (rimane logica Java, ma senza stili inline strani)
                    if (user.getProfilePicPath() != null && !user.getProfilePicPath().isEmpty()) {
                        try {
                            avatar.setFill(new ImagePattern(new Image(user.getProfilePicPath())));
                        } catch (Exception e) {
                            if (imageUrl != null) avatar.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
                        }
                    } else {
                        if (imageUrl != null) avatar.setFill(new ImagePattern(new Image(imageUrl.toExternalForm())));
                    }

                    // Info
                    VBox info = new VBox(2);
                    Label nameLbl = new Label(user.getName() + " " + user.getSurname());
                    nameLbl.getStyleClass().add("st-label-blue"); // Nome in blu/grassetto

                    Label emailLbl = new Label(user.getEmail());
                    emailLbl.getStyleClass().add("st-label-subtitle"); // Email in grigio

                    info.getChildren().addAll(nameLbl, emailLbl);

                    // Badge Ruolo
                    Label roleBadge = new Label(user.isAdmin() ? "ADMIN" : "USER");
                    // Assegna classi CSS invece di stili inline
                    if (user.isAdmin()) {
                        roleBadge.getStyleClass().add("st-badge-red");
                    } else {
                        roleBadge.getStyleClass().add("st-badge");
                    }

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Bottone Vedi Profilo
                    Button actionBtn = new Button("Vedi Profilo");
                    actionBtn.getStyleClass().add("st-button-secondary"); // Stile bottone standard
                    actionBtn.getStyleClass().add("st-button-small");     // Versione compatta

                    actionBtn.setOnAction(event -> {
                        try {
                            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("profiloUtente.fxml"));
                            javafx.scene.Parent root = loader.load();
                            UserProfileViewController controller = loader.getController();
                            controller.setTargetUser(user);

                            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
                            String css = getClass().getResource("/com/example/soundtribe/css/style.css").toExternalForm();
                            scene.getStylesheets().add(css);

                            javafx.stage.Stage stage = (javafx.stage.Stage) actionBtn.getScene().getWindow();
                            stage.setScene(scene);
                            stage.show();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    });

                    card.getChildren().addAll(avatar, info, spacer, roleBadge, actionBtn);
                    setGraphic(card);
                }
            }
        });
    }

    // --- LISTA RICHIESTE (SOSPESO) ---
    private void setupPendingListCellFactory() {
        richiesteSospeso.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox card = new HBox(10);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(8));

                    // Usa una classe CSS specifica per le card di "warning" o pending
                    // Assicurati di aggiungere .st-card-warning nel tuo CSS se vuoi lo sfondo giallino
                    // Altrimenti usa st-card standard
                    card.getStyleClass().add("st-card");
                    // card.setStyle("-fx-border-color: #f59e0b;"); // Opzionale: bordo arancione per distinguere

                    VBox info = new VBox(2);
                    Label nameLbl = new Label(user.getName() + " " + user.getSurname());
                    nameLbl.getStyleClass().add("st-label"); // Testo normale

                    Label emailLbl = new Label(user.getEmail());
                    emailLbl.getStyleClass().add("st-label-subtitle");

                    info.getChildren().addAll(nameLbl, emailLbl);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Bottoni Azione
                    Button btnApprove = new Button("✔ Accetta");
                    // Usa classi CSS (es. st-button-success se la crei, o primary)
                    btnApprove.getStyleClass().add("st-button-primary");
                    btnApprove.getStyleClass().add("st-button-small");

                    Button btnReject = new Button("✘ Rifiuta");
                    btnReject.getStyleClass().add("st-button-danger");
                    btnReject.getStyleClass().add("st-button-small");

                    btnApprove.setOnAction(e -> {
                        userDAO.updateUserStatus(user.getId(), true);
                        refreshData();
                        AlertUtil.mostra("Utente Approvato", "Successo", "L'utente " + user.getName() + " è ora attivo.", Alert.AlertType.INFORMATION);
                    });

                    btnReject.setOnAction(e -> {
                        userDAO.deleteUser(user.getId());
                        refreshData();
                        AlertUtil.mostra("Richiesta Rifiutata", "Utente rimosso", "La richiesta di registrazione è stata cancellata.", Alert.AlertType.INFORMATION);
                    });

                    card.getChildren().addAll(info, spacer, btnApprove, btnReject);
                    setGraphic(card);
                }
            }
        });
    }
}