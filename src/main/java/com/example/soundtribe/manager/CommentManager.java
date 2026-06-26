package com.example.soundtribe.manager;

import com.example.soundtribe.item.AlertUtil;
import com.example.soundtribe.item.UserSession;
import com.example.soundtribe.entita.Comment;
import com.example.soundtribe.dao.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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
    private final int currentUserId;

    public CommentManager(VBox commentsContainer, TextArea newCommentArea, Button btnPostComment,
                          int resourceId, ResourceType resourceType) {
        this.commentsContainer = commentsContainer;
        this.newCommentArea = newCommentArea;
        this.btnPostComment = btnPostComment;
        this.resourceId = resourceId;
        this.resourceType = resourceType;

        this.commentDAO = new CommentDAO();
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
                renderCommentRecursive(c, 0, null);
            }
        }
    }

    private void renderCommentRecursive(Comment c, int level, String parentAuthorName) {
        VBox commentBox = new VBox(6);
        commentBox.setPadding(new Insets(10, 12, 10, 12 + (level * 28)));

        if (level == 0) {
            commentBox.getStyleClass().add("st-card");
        } else {
            commentBox.setStyle(
                "-fx-border-color: " + levelBorderColor(level) + "; " +
                "-fx-border-width: 0 0 0 3; " +
                "-fx-background-color: " + levelBgColor(level) + "; " +
                "-fx-background-radius: 0 8 8 0;"
            );
        }

        // Header: avatar + autore (con eventuale "↳ risposta a") + spacer + like
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = buildAvatar(c.getAuthorName(), level);

        VBox authorCol = new VBox(1);
        authorCol.setAlignment(Pos.CENTER_LEFT);
        if (parentAuthorName != null) {
            Label replyTag = new Label("↳ risposta a " + parentAuthorName);
            replyTag.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px; -fx-font-style: italic;");
            authorCol.getChildren().add(replyTag);
        }
        Label userLbl = new Label(c.getAuthorName());
        userLbl.getStyleClass().add("st-label-blue");
        authorCol.getChildren().add(userLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

        header.getChildren().addAll(avatar, authorCol, spacer, likeBtn);

        Label contentLbl = new Label();
        contentLbl.setWrapText(true);

        Button replyBtn = new Button("Rispondi ↲");
        replyBtn.getStyleClass().addAll("st-button-secondary", "st-button-small");
        replyBtn.setStyle("-fx-border-color: transparent;");

        Button deleteBtn = new Button("🗑 Elimina");
        deleteBtn.getStyleClass().addAll("st-button-danger", "st-button-small");
        deleteBtn.setStyle("-fx-border-color: transparent;");

        boolean canDelete = commentDAO.canDeleteComment(c.getId(), currentUserId);
        deleteBtn.setVisible(canDelete);
        deleteBtn.setManaged(canDelete);

        deleteBtn.setOnAction(e -> {
            if (AlertUtil.chiediConferma("Conferma Eliminazione",
                    "Eliminare questo commento?",
                    "Questa azione non può essere annullata.")) {
                commentDAO.deleteComment(c.getId());
                loadComments();
            }
        });

        if ("Banned".equals(c.getStatus())) {
            contentLbl.setText("🚫 Questo commento è stato bannato dall'amministratore.");
            contentLbl.setStyle("-fx-font-style: italic; -fx-text-fill: #9ca3af; -fx-font-size: 11px;");
            likeBtn.setDisable(true);
            replyBtn.setDisable(true);
            deleteBtn.setDisable(true);
        } else if ("Reported".equals(c.getStatus())) {
            contentLbl.setText("⚠️ Questo commento è stato segnalato ed è in fase di revisione.");
            contentLbl.setStyle("-fx-font-style: italic; -fx-text-fill: #f59e0b; -fx-font-size: 11px;");
        } else {
            contentLbl.setText(c.getContent());
            contentLbl.getStyleClass().add("st-label");
        }

        HBox buttonsBox = new HBox(8);
        buttonsBox.getChildren().addAll(replyBtn, deleteBtn);

        VBox replyArea = new VBox(5);
        replyArea.setVisible(false);
        replyArea.setManaged(false);

        TextArea replyInput = new TextArea();
        replyInput.setPromptText("Rispondi a " + c.getAuthorName() + "...");
        replyInput.setPrefRowCount(2);
        replyInput.setWrapText(true);
        replyInput.getStyleClass().add("st-text-area");

        Button sendReplyBtn = new Button("Invia Risposta");
        sendReplyBtn.getStyleClass().addAll("st-button-primary", "st-button-small");
        sendReplyBtn.setOnAction(e -> {
            String replyText = replyInput.getText().trim();
            if (!replyText.isEmpty()) postReply(c.getId(), replyText);
        });

        replyArea.getChildren().addAll(replyInput, sendReplyBtn);

        replyBtn.setOnAction(e -> {
            boolean isVisible = !replyArea.isVisible();
            replyArea.setVisible(isVisible);
            replyArea.setManaged(isVisible);
            if (isVisible) replyInput.requestFocus();
        });

        commentBox.getChildren().addAll(header, contentLbl, buttonsBox, replyArea);
        commentsContainer.getChildren().add(commentBox);

        for (Comment reply : c.getReplies()) {
            renderCommentRecursive(reply, level + 1, c.getAuthorName());
        }
    }

    private StackPane buildAvatar(String name, int level) {
        StackPane stack = new StackPane();
        double radius = level == 0 ? 18 : 14;
        Circle bg = new Circle(radius);
        bg.setFill(Color.web(levelBorderColor(level)));
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
        Label lbl = new Label(initial);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: " + (level == 0 ? "14" : "11") + "px; -fx-font-weight: bold;");
        stack.getChildren().addAll(bg, lbl);
        return stack;
    }

    private String levelBorderColor(int level) {
        return switch (level) {
            case 0, 1 -> "#3969da";
            case 2    -> "#8a2be2";
            default   -> "#da3969";
        };
    }

    private String levelBgColor(int level) {
        return switch (level) {
            case 1  -> "#1a1a2e";
            case 2  -> "#1e1528";
            default -> "#1e1618";
        };
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
        int songId = (resourceType == ResourceType.SONG)      ? resourceId : 0;
        int execId = (resourceType == ResourceType.EXECUTION) ? resourceId : 0;
        int concId = (resourceType == ResourceType.CONCERT)   ? resourceId : 0;

        Comment newC = new Comment(0, songId, execId, concId, currentUserId, content, 0, parentId, "Pending");
        commentDAO.addComment(newC, resourceType, resourceId);
    }
}