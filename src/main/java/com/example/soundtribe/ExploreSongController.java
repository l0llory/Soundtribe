package com.example.soundtribe;

import com.example.soundtribe.dao.CommentDAO;
import com.example.soundtribe.dao.UserDAO;
import com.example.soundtribe.entità.Comment;
import com.example.soundtribe.entità.Song;
import com.example.soundtribe.entità.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color; // Import per i colori
import java.io.File;
import java.util.List;

public class ExploreSongController {

    // ... (Tutti i tuoi @FXML per bottoni e label rimangono uguali) ...
    @FXML public Button precP_Riproduci;
    @FXML public Button nextP_Riproduci;
    @FXML public Button backHome_Riproduci;
    @FXML public Button Exit_Riproduci;
    @FXML public Button backToListBtn;
    @FXML public Label titoloBranoLabel;
    @FXML public Label autoreBranoLabel;
    @FXML public Label genereBranoLabel;
    @FXML public Label annoBranoLabel;
    @FXML public Label uploaderLabel;
    @FXML public Label descrizioneLabel;
    @FXML public VBox coverContainer;
    @FXML public ImageView songCoverImageView;
    @FXML public Label defaultIconLabel;
    @FXML public VBox filesContainer;
    @FXML public Label noFilesLabel;

    @FXML public TextArea newCommentArea;
    @FXML public Button btnPostComment;
    @FXML public VBox commentsContainer; // Contenitore principale dei commenti

    private Song currentSong;
    private CommentDAO commentDAO;
    private UserDAO userDAO;
    private int currentUserId; // Salviamo l'ID utente loggato

    @FXML
    public void initialize() {
        commentDAO = new CommentDAO();
        userDAO = new UserDAO();
        currentUserId = UserSession.getInstance().getUserId(); // Prendi ID dalla sessione

        NavigationManager.updateNavigationButtons(precP_Riproduci, nextP_Riproduci);
        NavigationManager.navBack(precP_Riproduci);
        NavigationManager.navForward(nextP_Riproduci);
        NavigationManager.home(backHome_Riproduci);
        NavigationManager.exit(Exit_Riproduci);

        backToListBtn.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true));
        btnPostComment.setOnAction(e -> postNewRootComment());
    }

    public void setSong(Song song) {
        this.currentSong = song;

        // ... (Logica di set testo e cover invariata) ...
        titoloBranoLabel.setText(song.getTitle());
        autoreBranoLabel.setText("Autore: " + song.getArtist());
        genereBranoLabel.setText(song.getGenre().toUpperCase());
        annoBranoLabel.setText("");
        if (song.getUploaderName() != null) uploaderLabel.setText("Caricato da: " + song.getUploaderName() + " " + song.getUploaderSurname());
        else uploaderLabel.setText("Caricato da: Utente sconosciuto");
        if (song.getDescription() != null) descrizioneLabel.setText(song.getDescription());
        else descrizioneLabel.setText("Nessuna descrizione disponibile.");

        loadCover(song);
        loadAttachedFiles();

        // Caricamento Commenti Aggiornato
        loadComments();
    }

    // --- NUOVA LOGICA COMMENTI (ALBERO + LIKE TOGGLE) ---

    private void loadComments() {
        commentsContainer.getChildren().clear();

        // Recuperiamo i TOP 3 commenti radice con i loro figli già caricati
        List<Comment> rootComments = commentDAO.getTop3RootComments(currentSong.getId());

        if (rootComments.isEmpty()) {
            Label placeholder = new Label("Nessun commento ancora. Sii il primo!");
            placeholder.setTextFill(Color.GRAY);
            commentsContainer.getChildren().add(placeholder);
        } else {
            for (Comment c : rootComments) {
                // Renderizza ricorsivamente partendo dal livello 0
                renderCommentRecursive(c, 0);
            }
        }
    }

    // Metodo ricorsivo per disegnare commenti e risposte
    private void renderCommentRecursive(Comment c, int level) {
        // Container del singolo commento
        VBox commentBox = new VBox(5);

        // INDENTAZIONE: Margine sinistro aumenta col livello
        // Level 0 = 0px, Level 1 = 30px, Level 2 = 60px...
        commentBox.setPadding(new Insets(10, 10, 10, 10 + (level * 30)));

        // Stile visivo: Solo i commenti radice (level 0) hanno lo sfondo bianco "card"
        // Le risposte hanno uno sfondo trasparente o leggermente diverso
        if (level == 0) {
            commentBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        } else {
            commentBox.setStyle("-fx-background-color: transparent; -fx-border-color: #eeeeee; -fx-border-width: 0 0 0 2;"); // Linea guida a sinistra
        }

        // --- HEADER (Nome + Like) ---
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userLbl = new Label(c.getUsername());
        userLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        userLbl.setTextFill(Color.web("#3969da"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bottone Like (Cuore)
        Button likeBtn = new Button();
        boolean isLiked = commentDAO.hasUserLiked(currentUserId, c.getId());
        updateLikeButtonStyle(likeBtn, isLiked, c.getLikes());

        likeBtn.setOnAction(e -> {
            // Logica Toggle
            commentDAO.toggleLike(currentUserId, c.getId());
            // Ricarica TUTTO per aggiornare conteggi e stati (modo più semplice per sincronizzare)
            loadComments();
        });

        header.getChildren().addAll(userLbl, spacer, likeBtn);

        // --- CONTENUTO ---
        Label contentLbl = new Label(c.getContent());
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-text-fill: #333333;");

        // --- BOTTONE RISPONDI ---
        Button replyBtn = new Button("Rispondi ↲");
        replyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #908888; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 0 5 0;");

        // Area di risposta (nascosta di default)
        VBox replyArea = new VBox(5);
        replyArea.setVisible(false);
        replyArea.setManaged(false); // Non occupa spazio quando nascosta

        TextArea replyInput = new TextArea();
        replyInput.setPromptText("Rispondi a " + c.getUsername() + "...");
        replyInput.setPrefRowCount(2);
        replyInput.setWrapText(true);

        Button sendReplyBtn = new Button("Invia Risposta");
        sendReplyBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-font-size: 11px;");

        sendReplyBtn.setOnAction(e -> {
            String replyText = replyInput.getText().trim();
            if (!replyText.isEmpty()) {
                postReply(c.getId(), replyText); // Posta risposta con ID padre
            }
        });

        replyArea.getChildren().addAll(replyInput, sendReplyBtn);

        // Azione mostra/nascondi risposta
        replyBtn.setOnAction(e -> {
            boolean isVisible = !replyArea.isVisible();
            replyArea.setVisible(isVisible);
            replyArea.setManaged(isVisible);
            if(isVisible) replyInput.requestFocus();
        });

        commentBox.getChildren().addAll(header, contentLbl, replyBtn, replyArea);

        // Aggiungi questo box al container principale
        commentsContainer.getChildren().add(commentBox);

        // --- RICORSIONE: Disegna i figli ---
        // Nota: i figli non vengono messi DENTRO il box del padre, ma sotto,
        // nel container principale, però con indentazione aumentata.
        for (Comment reply : c.getReplies()) {
            renderCommentRecursive(reply, level + 1);
        }
    }

    private void updateLikeButtonStyle(Button btn, boolean isLiked, int count) {
        if (isLiked) {
            btn.setText("❤ " + count); // Cuore pieno
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else {
            btn.setText("♡ " + count); // Cuore vuoto
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #908888; -fx-cursor: hand; -fx-font-size: 13px;");
        }
    }

    private void postNewRootComment() {
        String content = newCommentArea.getText().trim();
        if (content.isEmpty()) return;

        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        // Parent ID è null perché è un commento radice
        Comment newC = new Comment(0, currentSong.getId(), currentUserId, username, content, 0, null);
        commentDAO.addComment(newC);

        newCommentArea.clear();
        loadComments();
    }

    private void postReply(int parentId, String content) {
        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        // Qui passiamo parentId!
        Comment reply = new Comment(0, currentSong.getId(), currentUserId, username, content, 0, parentId);
        commentDAO.addComment(reply);

        loadComments(); // Ricarica tutto per mostrare la nuova risposta
    }

    // --- METODI DI SUPPORTO (Invariati) ---
    private void loadCover(Song song) {
        if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
            try {
                Image img = new Image(song.getCoverPath());
                if (!img.isError()) {
                    songCoverImageView.setImage(img);
                    songCoverImageView.setVisible(true);
                    songCoverImageView.setManaged(true);
                    defaultIconLabel.setVisible(false);
                    defaultIconLabel.setManaged(false);
                    Rectangle clip = new Rectangle(100, 100);
                    clip.setArcWidth(10); clip.setArcHeight(10);
                    songCoverImageView.setClip(clip);
                } else showDefaultIcon();
            } catch (Exception e) { showDefaultIcon(); }
        } else showDefaultIcon();
    }

    private void showDefaultIcon() {
        songCoverImageView.setVisible(false);
        songCoverImageView.setManaged(false);
        defaultIconLabel.setVisible(true);
        defaultIconLabel.setManaged(true);
    }

    private void loadAttachedFiles() {
        filesContainer.getChildren().clear();
        boolean hasFiles = false;
        if (currentSong.getPdfSheetPath() != null && !currentSong.getPdfSheetPath().isEmpty()) {
            addFileButton("📄 Visualizza Spartito/Testo (PDF)", currentSong.getPdfSheetPath(), false);
            hasFiles = true;
        }
        if (currentSong.getAudioPath() != null && !currentSong.getAudioPath().isEmpty()) {
            addFileButton("🎵 Riproduci Audio", currentSong.getAudioPath(), false);
            hasFiles = true;
        }
        if (currentSong.getYoutubeUrl() != null && !currentSong.getYoutubeUrl().isEmpty()) {
            addFileButton("📺 Guarda su YouTube", currentSong.getYoutubeUrl(), true);
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
        fileRow.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dddddd; -fx-border-radius: 8;");
        fileRow.setAlignment(Pos.CENTER_LEFT);
        Label descLabel = new Label(labelText);
        descLabel.setFont(new Font(14));
        descLabel.setStyle("-fx-text-fill: #333333;");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button actionBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        actionBtn.setStyle("-fx-background-color: #3969da; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        actionBtn.setOnAction(e -> {
            if (isUrl) handleOpenUrl(pathOrUrl);
            else handleOpenFile(pathOrUrl);
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

    public static void handleOpenUrl(String url) {
        try { Launcher.getInstance().openDocument(url); } catch (Exception e) { e.printStackTrace(); }
    }
}