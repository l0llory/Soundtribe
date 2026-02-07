package com.example.soundtribe.entità;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private int id;
    private int songId;
    private int userId;
    private String username; // Per visualizzare chi ha scritto senza fare troppe query
    private String content;
    private int likes;
    private Integer parentId; // Può essere null se è un commento principale

    // Lista per contenere le risposte (figli)
    private List<Comment> replies = new ArrayList<>();

    public Comment() {}

    public Comment(int id, int songId, int userId, String username, String content, int likes, Integer parentId) {
        this.id = id;
        this.songId = songId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.likes = likes;
        this.parentId = parentId;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSongId() { return songId; }
    public void setSongId(int songId) { this.songId = songId; }

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

    public List<Comment> getReplies() { return replies; }
    public void addReply(Comment reply) { this.replies.add(reply); }
}