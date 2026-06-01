package com.example.soundtribe.entità;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private int id;
    private int userId;
    private String username;
    private String content;
    private int likes;
    private Integer parentId; // Null se è un commento principale
    private String status;

    // Riferimenti alle Risorse (Uno di questi sarà valorizzato, gli altri 0/null)
    private int songId;
    private int executionId;
    private int concertId;

    // Lista per contenere le risposte (figli)
    private List<Comment> replies = new ArrayList<>();

    // Costruttore Vuoto
    //public Comment(int id, int songId, int i, int i1, int userId, String username, String content, int likes, Integer parentId, String status) {}

    // Costruttore Completo (Aggiornato con tutti i tipi di risorsa)
    public Comment(int id, int songId, int executionId, int concertId, int userId, String username,
                   String content, int likes, Integer parentId, String status) {
        this.id = id;
        this.songId = songId;
        this.executionId = executionId;
        this.concertId = concertId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.likes = likes;
        this.parentId = parentId;
        this.status = status != null ? status : "Pending";    }

    // Costruttore di compatibilità (Utile se crei commenti al volo solo per Canzoni)
    public Comment(int id, int songId, int userId, String username, String content, int likes, Integer parentId, String status) {
        this(id, songId, 0, 0, userId, username, content, likes, parentId, status);
    }

    // --- GETTERS E SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }


    public int getSongId() { return songId; }
    public void setSongId(int songId) { this.songId = songId; }

    public int getExecutionId() { return executionId; }
    public void setExecutionId(int executionId) { this.executionId = executionId; }

    public int getConcertId() { return concertId; }
    public void setConcertId(int concertId) { this.concertId = concertId; }

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    // --- Gestione Risposte ---

    public List<Comment> getReplies() { return replies; }

    // Metodo fondamentale usato dal DAO per settare la lista completa
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    public void addReply(Comment reply) {
        this.replies.add(reply);
    }
}