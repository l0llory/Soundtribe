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

import java.io.File;
import java.util.List;

public class ExploreSongController {

    // FXML Elements
    @FXML public Button precP_Riproduci;
    @FXML public Button nextP_Riproduci;
    @FXML public Button backHome_Riproduci;
    @FXML public Button Exit_Riproduci;
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
    private int currentUserId; // ID utente loggato

    @FXML
    public void initialize() {
        commentDAO = new CommentDAO();
        userDAO = new UserDAO();
        currentUserId = UserSession.getInstance().getUserId();

        // Setup Navigazione
        NavigationManager.navBack(precP_Riproduci);
        NavigationManager.navForward(nextP_Riproduci);
        NavigationManager.updateNavigationButtons(precP_Riproduci, nextP_Riproduci);
        NavigationManager.home(backHome_Riproduci);
        NavigationManager.exit(Exit_Riproduci);

        precP_Riproduci.setOnAction(event -> SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true));
        btnPostComment.setOnAction(e -> postNewRootComment());
    }

    public void setSong(Song song) {
        this.currentSong = song;

        // Popolamento UI (testi gestiti da CSS nel FXML)
        titoloBranoLabel.setText(song.getTitle());
        autoreBranoLabel.setText("Autore: " + song.getArtist());
        genereBranoLabel.setText(song.getGenre().toUpperCase());
        annoBranoLabel.setText(""); // Se hai l'anno, mettilo qui

        if (song.getUploaderName() != null)
            uploaderLabel.setText("Caricato da: " + song.getUploaderName() + " " + song.getUploaderSurname());
        else
            uploaderLabel.setText("Caricato da: Utente sconosciuto");

        if (song.getDescription() != null && !song.getDescription().isEmpty())
            descrizioneLabel.setText(song.getDescription());
        else
            descrizioneLabel.setText("Nessuna descrizione disponibile.");

        loadCover(song);
        loadAttachedFiles();
        loadComments();
    }

    // --- LOGICA COMMENTI (CSS APPLIED) ---

    private void loadComments() {
        commentsContainer.getChildren().clear();

        // Recuperiamo i TOP commenti radice
        List<Comment> rootComments = commentDAO.getTop3RootComments(currentSong.getId());

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

    // Metodo ricorsivo per disegnare commenti e risposte
    private void renderCommentRecursive(Comment c, int level) {
        VBox commentBox = new VBox(5);

        // Indentazione per le risposte
        commentBox.setPadding(new Insets(10, 10, 10, 10 + (level * 20)));

        // Stile visivo: Root = Card, Reply = Transparent con bordo sinistro
        if (level == 0) {
            commentBox.getStyleClass().add("st-card"); // Sfondo scuro per il commento principale
        } else {
            // Per le risposte usiamo uno stile più leggero o nulla
            // Opzionale: aggiungi una classe .st-reply-box nel CSS per gestire il bordo sinistro
            commentBox.setStyle("-fx-border-color: #2d2d2d; -fx-border-width: 0 0 0 2;");
        }

        // HEADER (Nome + Like)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userLbl = new Label(c.getUsername());
        userLbl.getStyleClass().add("st-label-blue"); // Nome utente evidenziato

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bottone Like
        Button likeBtn = new Button();
        boolean isLiked = commentDAO.hasUserLiked(currentUserId, c.getId());
        updateLikeButtonStyle(likeBtn, isLiked, c.getLikes());

        likeBtn.setOnAction(e -> {
            commentDAO.toggleLike(currentUserId, c.getId());
            loadComments(); // Ricarica per aggiornare stato e contatore
        });

        header.getChildren().addAll(userLbl, spacer, likeBtn);

        // CONTENUTO
        Label contentLbl = new Label(c.getContent());
        contentLbl.setWrapText(true);
        contentLbl.getStyleClass().add("st-label"); // Testo standard (bianco/grigio chiaro)

        // BOTTONE RISPONDI
        Button replyBtn = new Button("Rispondi ↲");
        replyBtn.getStyleClass().add("st-button-secondary"); // Stile outline
        replyBtn.getStyleClass().add("st-button-small");     // Piccolo
        replyBtn.setStyle("-fx-border-color: transparent;"); // Rimuovi bordo se vuoi solo testo

        // AREA RISPOSTA (Nascosta)
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

        // Toggle Risposta
        replyBtn.setOnAction(e -> {
            boolean isVisible = !replyArea.isVisible();
            replyArea.setVisible(isVisible);
            replyArea.setManaged(isVisible);
            if(isVisible) replyInput.requestFocus();
        });

        commentBox.getChildren().addAll(header, contentLbl, replyBtn, replyArea);
        commentsContainer.getChildren().add(commentBox);

        // RICORSIONE FIGLI
        for (Comment reply : c.getReplies()) {
            renderCommentRecursive(reply, level + 1);
        }
    }

    private void updateLikeButtonStyle(Button btn, boolean isLiked, int count) {
        if (isLiked) {
            btn.setText("❤ " + count);
            btn.getStyleClass().removeAll("st-button-outline"); // Rimuovi stili precedenti
            btn.getStyleClass().add("st-button-danger");        // Usa stile rosso (danger) per il like attivo
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); // Solo testo rosso
        } else {
            btn.setText("♡ " + count);
            btn.getStyleClass().removeAll("st-button-danger");
            btn.getStyleClass().add("st-button-outline");       // Usa stile outline per like inattivo
            btn.setStyle("-fx-border-color: transparent;");     // Solo testo grigio
        }
    }

    private void postNewRootComment() {
        String content = newCommentArea.getText().trim();
        if (content.isEmpty()) return;

        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        Comment newC = new Comment(0, currentSong.getId(), currentUserId, username, content, 0, null);
        commentDAO.addComment(newC);

        newCommentArea.clear();
        loadComments();
    }

    private void postReply(int parentId, String content) {
        User me = userDAO.getUserById(currentUserId);
        String username = (me != null) ? me.getName() : "Utente";

        Comment reply = new Comment(0, currentSong.getId(), currentUserId, username, content, 0, parentId);
        commentDAO.addComment(reply);

        loadComments();
    }

    // --- GESTIONE FILE & COVER ---

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
            addFileButton("📄 Visualizza Spartito (PDF)", currentSong.getPdfSheetPath(), false);
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
        // Container File: Usa st-card
        HBox fileRow = new HBox(15);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        fileRow.getStyleClass().add("st-card");
        // Nota: st-card ha molto padding, se è troppo per una riga file, crea .st-panel-row nel CSS
        fileRow.setPadding(new Insets(15)); // Override padding se necessario

        Label descLabel = new Label(labelText);
        descLabel.getStyleClass().add("st-label"); // Testo standard

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button(isUrl ? "Apri Link" : "Apri File");
        actionBtn.getStyleClass().add("st-button-primary"); // Bottone blu
        actionBtn.getStyleClass().add("st-button-small");

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