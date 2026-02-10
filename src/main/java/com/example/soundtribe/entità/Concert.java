package com.example.soundtribe.entità;

import java.sql.Date;

public class Concert {
    private int id;
    private String title;
    private String artist;
    private String youtubeUrl;
    private Date date;
    private String location;
    private String description; // Qui l'utente può incollare la scaletta/tracklist testuale per ora
    private int uploaderId;

    public Concert(int id, String title, String artist, String youtubeUrl, Date date, String location, String description, int uploaderId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.youtubeUrl = youtubeUrl;
        this.date = date;
        this.location = location;
        this.description = description;
        this.uploaderId = uploaderId;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public Date getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public int getUploaderId() { return uploaderId; }
}