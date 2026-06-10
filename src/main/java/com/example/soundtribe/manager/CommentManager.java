package com.example.soundtribe.manager;

import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.entita.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class CommentManager {

    public enum ResourceType {
        SONG, EXECUTION, CONCERT
    }

    private final VBox commentsContainer;
    private final TextArea newCommentArea;
    private final Button btnPostComment;
    private final int resourceId;
    private final ResourceType resourceType;

    private final CommentDAO commentDAO;
    private final UserDAO userDAO;
    private final int currentUserId;

    public CommentManager(VBox commentsContainer, TextArea newCommentArea, Button btnPostComment,
                          int resourceId, ResourceType resourceType) {
        this.commentsContainer = commentsContainer;
        this.newCommentArea = newCommentArea;
        this.btnPostComment = btnPostComment;
        this.resourceId = resourceId;
        this.resourceType = resourceType;

        this.commentDAO = new CommentDAO();
        this.userDAO = new UserDAO();
        this.currentUserId = UserSession.getInstance().getUserId();

        init();
    }

    private void init() {
        btnPostComment.setOnAction(e -> postNewRootComment());
        loadComments();
    }

    public void loadComments() {
        commentsContainer.getChildren().clear();

        List<Comment> rootComments = commentDAO.getCommentsByResource(resourceType, resourceId);

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

        // Like Button con cuore pieno/vuoto
        Button likeBtn = new Button();
        boolean isLiked = commentDAO.hasUserLiked(currentUserId, c.getId());
        updateLikeButtonStyle(likeBtn, isLiked, c.getLikes());

        likeBtn.setOnAction(e -> {
            if (commentDAO.hasUserLiked(currentUserId, c.getId())) {
                commentDAO.removeLike(currentUserId, c.getId());
            } else {
                commentDAO.toggleLike(currentUserId, c.getId());
            }
            loadComments();
        });

        header.getChildren().addAll(userLbl, spacer, likeBtn);

        // Content (Inizializzato vuoto, verrà popolato in base allo status)
        Label contentLbl = new Label();
        contentLbl.setWrapText(true);

        // Reply Button
        Button replyBtn = new Button("Rispondi ↲");
        replyBtn.getStyleClass().addAll("st-button-secondary", "st-button-small");
        replyBtn.setStyle("-fx-border-color: transparent;");

        // DELETE Button (Mostra solo se sei il proprietario della risorsa)
        Button deleteBtn = new Button("🗑 Elimina");
        deleteBtn.getStyleClass().addAll("st-button-danger", "st-button-small");
        deleteBtn.setStyle("-fx-border-color: transparent;");

        // Controlla se l'utente corrente è il proprietario della risorsa
        boolean canDelete = commentDAO.canDeleteComment(c.getId(), currentUserId);
        deleteBtn.setVisible(canDelete);
        deleteBtn.setManaged(canDelete);

        deleteBtn.setOnAction(e -> {
            // Chiedi conferma
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma Eliminazione");
            alert.setHeaderText("Eliminare questo commento?");
            alert.setContentText("Questa azione non può essere annullata.");

            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                commentDAO.deleteComment(c.getId());
                loadComments(); // Ricarica la lista
            }
        });


        if ("Banned".equals(c.getStatus())) {
            contentLbl.setText("🚫 Questo commento è stato bannato dall'amministratore.");
            contentLbl.setStyle(
                    "-fx-font-style: italic; " +
                            "-fx-text-fill: #9ca3af; " + // Grigio chiaro
                            "-fx-font-size: 11px;"
            );
            // Disabilita le interazioni per i commenti bannati
            likeBtn.setDisable(true);
            replyBtn.setDisable(true);
            deleteBtn.setDisable(true);

        } else if ("Reported".equals(c.getStatus())) {
            contentLbl.setText("⚠️ Questo commento è stato segnalato ed è in fase di revisione.");
            contentLbl.setStyle(
                    "-fx-font-style: italic; " +
                            "-fx-text-fill: #f59e0b; " + // Giallo/Arancione ambra per l'avviso
                            "-fx-font-size: 11px;"
            );
            // Lasciamo i bottoni attivi nel caso di una semplice segnalazione, cambia solo il font

        } else {
            // Stato standard (Verified o Pending)
            contentLbl.setText(c.getContent());
            contentLbl.getStyleClass().add("st-label"); // Ripristina lo stile CSS globale del progetto
        }
        // =================================================================

        // HBox per i bottoni (Reply e Delete)
        HBox buttonsBox = new HBox(8);
        buttonsBox.getChildren().addAll(replyBtn, deleteBtn);

        // Reply Area (Hidden)
        VBox replyArea = new VBox(5);
        replyArea.setVisible(false);
        replyArea.setManaged(false);

        TextArea replyInput = new TextArea();
        replyInput.setPromptText("Rispondi a " + c.getUsername() + "...");
        replyInput.setPrefRowCount(2);
        replyInput.setWrapText(true);
        replyInput.getStyleClass().add("st-text-area");

        Button sendReplyBtn = new Button("Invia Risposta");
        sendReplyBtn.getStyleClass().addAll("st-button-primary", "st-button-small");

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

        commentBox.getChildren().addAll(header, contentLbl, buttonsBox, replyArea);
        commentsContainer.getChildren().add(commentBox);

        for (Comment reply : c.getReplies()) {
            renderCommentRecursive(reply, level + 1);
        }
    }

    private void updateLikeButtonStyle(Button btn, boolean isLiked, int count) {
        if (isLiked) {
            // Cuore pieno ❤
            btn.setText("❤ " + count);
            btn.getStyleClass().removeAll("st-button-outline");
            btn.getStyleClass().add("st-button-danger");
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        } else {
            // Cuore vuoto ♡
            btn.setText("♡ " + count);
            btn.getStyleClass().removeAll("st-button-danger");
            btn.getStyleClass().add("st-button-outline");
            btn.setStyle("-fx-border-color: transparent;");
        }
    }

    private void postNewRootComment() {
        String content = newCommentArea.getText().trim();
        if (content.isEmpty()) return;
        saveComment(content, null);
        newCommentArea.clear();
        loadComments();
    }

    private void postReply(int parentId, String content) {
        saveComment(content, parentId);
        loadComments();
    }

    private void saveComment(String content, Integer parentId) {
        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        int songId = (resourceType == ResourceType.SONG) ? resourceId : 0;
        int execId = (resourceType == ResourceType.EXECUTION) ? resourceId : 0;
        int concId = (resourceType == ResourceType.CONCERT) ? resourceId : 0;

        Comment newC = new Comment(0, songId, execId, concId, currentUserId, username, content, 0, parentId, "Pending");

        commentDAO.addComment(newC, resourceType, resourceId);
    }
}