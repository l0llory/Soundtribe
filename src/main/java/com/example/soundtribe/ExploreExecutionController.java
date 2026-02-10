package com.example.soundtribe;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Comment;
import com.example.soundtribe.entità.Esecution;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ExploreExecutionController {

    // Bottoni Navigazione
    @FXML public Button precP_Exec;
    @FXML public Button nextP_Exec;
    @FXML public Button backHome_Exec;
    @FXML public Button Exit_Exec;

    // Labels Header
    @FXML public Label titleLabel;
    @FXML public Label executorsLabel;
    @FXML public Label uploaderLabel;
    @FXML public Label typeLabel;
    @FXML public Label dateLabel;
    @FXML public Label placeLabel;

    // Dettagli Tecnici
    @FXML public Label instrumentsLabel;

    // File
    @FXML public VBox filesContainer;
    @FXML public Label noFilesLabel;

    // Commenti
    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer;

    private Esecution currentExecution;
    private CommentDAO commentDAO;
    private UserDAO userDAO;
    private int currentUserId;

    @FXML
    public void initialize() {
        commentDAO = new CommentDAO();
        userDAO = new UserDAO();
        currentUserId = UserSession.getInstance().getUserId();

        // Setup Navigazione
        NavigationManager.navBack(precP_Exec);
        NavigationManager.navForward(nextP_Exec);
        NavigationManager.updateNavigationButtons(precP_Exec, nextP_Exec);
        NavigationManager.home(backHome_Exec);
        NavigationManager.exit(Exit_Exec);

        // Tornare indietro riporta alla lista brani
        precP_Exec.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true));

        btnPostComment.setOnAction(e -> postNewRootComment());
    }

    public void setExecution(Esecution execution) {
        this.currentExecution = execution;

        // Popolamento UI
        titleLabel.setText(execution.getTitle());
        executorsLabel.setText("Esecutori: " + execution.getExecutors());
        instrumentsLabel.setText(execution.getInstruments());
        placeLabel.setText(execution.getRecordingPlace() != null ? execution.getRecordingPlace() : "-");
        dateLabel.setText(execution.getRecordingDate() != null ? execution.getRecordingDate().toString() : "-");

        // Info Uploader (Simulazione: EsecutionDAO dovrebbe recuperare nome uploader o farlo via UserDAO)
        // Se l'oggetto Esecution non ha i campi nome/cognome uploader, facciamo una query veloce
        if (execution.getUploaderId() > 0) {
            User u = userDAO.getUserById(execution.getUploaderId());
            if (u != null) {
                uploaderLabel.setText("Caricato da: " + u.getName() + " " + u.getSurname());
            } else {
                uploaderLabel.setText("Caricato da: Utente " + execution.getUploaderId());
            }
        } else {
            uploaderLabel.setText("Caricato da: Sconosciuto");
        }

        // Tipo
        String typeInfo = "";
        if (execution.isLive()) typeInfo += "LIVE"; else typeInfo += "STUDIO";
        if (execution.getSongId() == 0) typeInfo += " • INEDITO"; else typeInfo += " • COVER";
        typeLabel.setText(typeInfo);

        loadAttachedFiles();
        loadComments();
    }

    // --- LOGICA FILE ALLEGATI ---

    private void loadAttachedFiles() {
        filesContainer.getChildren().clear();
        boolean hasFiles = false;

        String path = currentExecution.getFilePath();
        String type = currentExecution.getFileType();

        if (path != null && !path.isEmpty()) {
            boolean isUrl = path.startsWith("http") || path.startsWith("www");
            String label = "File (" + (type != null ? type : "Media") + ")";
            if (isUrl) label = "Link Esterno / YouTube";

            addFileButton(label, path, isUrl);
            hasFiles = true;
        }

        if (!hasFiles) {
            noFilesLabel.setVisible(true);
            filesContainer.getChildren().add(noFilesLabel);
        } else {
            noFilesLabel.setVisible(false);
        }
    }

    private void addFileButton(String labelText, String pathOrUrl, boolean isUrl) {
        HBox fileRow = new HBox(15);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        fileRow.getStyleClass().add("st-card");
        fileRow.setPadding(new Insets(15));

        Label descLabel = new Label(labelText);
        descLabel.getStyleClass().add("st-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        actionBtn.getStyleClass().add("st-button-primary");
        actionBtn.getStyleClass().add("st-button-small");

        actionBtn.setOnAction(e -> {
            if (isUrl) {
                // CORREZIONE: Usiamo l'istanza singleton invece del metodo statico inesistente
                try {
                    Launcher.getInstance().openDocument(pathOrUrl);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                handleOpenFile(pathOrUrl);
            }
        });

        fileRow.getChildren().addAll(descLabel, spacer, actionBtn);
        filesContainer.getChildren().add(fileRow);
    }

    private void handleOpenFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) Launcher.getInstance().openDocument(file.toURI().toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- LOGICA COMMENTI ---

    private void loadComments() {
        commentsContainer.getChildren().clear();

        // NOTA: Qui stiamo usando l'ID dell'esecuzione come se fosse un songId.
        // Assicurati che il DB supporti commenti per le esecuzioni o che usiamo una tabella unificata.
        List<Comment> rootComments = commentDAO.getTop3RootComments(currentExecution.getId()); // Adattare se necessario

        if (rootComments.isEmpty()) {
            Label placeholder = new Label("Nessun commento ancora. Sii il primo!");
            placeholder.getStyleClass().add("st-label-subtitle");
            commentsContainer.getChildren().add(placeholder);
        } else {
            for (Comment c : rootComments) {
                renderCommentRecursive(c, 0);
            }
        }
    }

    private void renderCommentRecursive(Comment c, int level) {
        VBox commentBox = new VBox(5);
        commentBox.setPadding(new Insets(10, 10, 10, 10 + (level * 20)));

        if (level == 0) {
            commentBox.getStyleClass().add("st-card");
        } else {
            commentBox.setStyle("-fx-border-color: #2d2d2d; -fx-border-width: 0 0 0 2;");
        }

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label userLbl = new Label(c.getUsername());
        userLbl.getStyleClass().add("st-label-blue");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button likeBtn = new Button();

        boolean isLiked = commentDAO.hasUserLiked(currentUserId, c.getId());
        updateLikeButtonStyle(likeBtn, isLiked, c.getLikes());

        likeBtn.setOnAction(e -> {
            commentDAO.toggleLike(currentUserId, c.getId());
            loadComments();
        });

        header.getChildren().addAll(userLbl, spacer, likeBtn);

        // Content
        Label contentLbl = new Label(c.getContent());
        contentLbl.setWrapText(true);
        contentLbl.getStyleClass().add("st-label");

        // Reply
        Button replyBtn = new Button("Rispondi ↲");
        replyBtn.getStyleClass().add("st-button-secondary");
        replyBtn.getStyleClass().add("st-button-small");
        replyBtn.setStyle("-fx-border-color: transparent;");

        VBox replyArea = new VBox(5);
        replyArea.setVisible(false);
        replyArea.setManaged(false);

        TextArea replyInput = new TextArea();
        replyInput.setPromptText("Rispondi a " + c.getUsername() + "...");
        replyInput.setPrefRowCount(2);
        replyInput.setWrapText(true);
        replyInput.getStyleClass().add("st-text-area");

        Button sendReplyBtn = new Button("Invia Risposta");
        sendReplyBtn.getStyleClass().add("st-button-primary");
        sendReplyBtn.getStyleClass().add("st-button-small");

        sendReplyBtn.setOnAction(e -> {
            String replyText = replyInput.getText().trim();
            if (!replyText.isEmpty()) {
                postReply(c.getId(), replyText);
            }
        });

        replyArea.getChildren().addAll(replyInput, sendReplyBtn);

        replyBtn.setOnAction(e -> {
            boolean isVisible = !replyArea.isVisible();
            replyArea.setVisible(isVisible);
            replyArea.setManaged(isVisible);
            if(isVisible) replyInput.requestFocus();
        });

        commentBox.getChildren().addAll(header, contentLbl, replyBtn, replyArea);
        commentsContainer.getChildren().add(commentBox);

        for (Comment reply : c.getReplies()) {
            renderCommentRecursive(reply, level + 1);
        }
    }

    private void updateLikeButtonStyle(Button btn, boolean isLiked, int count) {
        if (isLiked) {
            btn.setText("❤ " + count);
            btn.getStyleClass().removeAll("st-button-outline");
            btn.getStyleClass().add("st-button-danger");
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        } else {
            btn.setText("♡ " + count);
            btn.getStyleClass().removeAll("st-button-danger");
            btn.getStyleClass().add("st-button-outline");
            btn.setStyle("-fx-border-color: transparent;");
        }
    }

    private void postNewRootComment() {
        String content = newCommentArea.getText().trim();
        if (content.isEmpty()) return;

        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        // NOTA: Passiamo l'ID esecuzione al posto dell'ID canzone
        Comment newC = new Comment(0, currentExecution.getId(), currentUserId, username, content, 0, null);
        commentDAO.addComment(newC);

        newCommentArea.clear();
        loadComments();
    }

    private void postReply(int parentId, String content) {
        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        Comment reply = new Comment(0, currentExecution.getId(), currentUserId, username, content, 0, parentId);
        commentDAO.addComment(reply);

        loadComments();
    }
}