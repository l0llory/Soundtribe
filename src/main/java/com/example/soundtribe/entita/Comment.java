package com.example.soundtribe.entita;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private int id;
    private int userId;
    private String authorName; // derivato via JOIN con users, non salvato nel DB
    private String content;
    private int likes;
    private Integer parentId;
    private String status;

    private int songId;
    private int executionId;
    private int concertId;

    private List<Comment> replies = new ArrayList<>();

    public Comment(int id, int songId, int executionId, int concertId, int userId,
                   String content, int likes, Integer parentId, String status) {
        this.id = id;
        this.songId = songId;
        this.executionId = executionId;
        this.concertId = concertId;
        this.userId = userId;
        this.content = content;
        this.likes = likes;
        this.parentId = parentId;
        this.status = status != null ? status : "Pending";
    }

    // --- GETTERS E SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }
    public void addReply(Comment reply) { this.replies.add(reply); }
}
