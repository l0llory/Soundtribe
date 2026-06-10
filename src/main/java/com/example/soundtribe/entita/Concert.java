package com.example.soundtribe.entita;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Concert {
    private int id;
    private String title;
    private String artist;
    private String youtubeUrl;
    private Date date;
    private String location;
    private String description;
    private int uploaderId;

    // NUOVO: Lista strutturata delle tracce eseguite
    public List<ConcertTrack> tracks;

    public Concert(int id, String title, String artist, String youtubeUrl, Date date, String location, String description, int uploaderId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.youtubeUrl = youtubeUrl;
        this.date = date;
        this.location = location;
        this.description = description;
        this.uploaderId = uploaderId;
        this.tracks = new ArrayList<>();
    }

    // Getters e Setters originali
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public Date getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public int getUploaderId() { return uploaderId; }

    // Nuovi metodi per la gestione della scaletta
    public List<ConcertTrack> getTracks() { return tracks; }
    public void setTracks(List<ConcertTrack> tracks) { this.tracks = tracks; }
    public void addTrack(ConcertTrack track) { this.tracks.add(track); }
}
