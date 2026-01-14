package com.example.soundtribe;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private String pdfSheetPath;
    private String audioPath;
    private String youtubeUrl;
    private String coverPath; // Nuovo campo per l'immagine

    public Song() {}

    public Song(int id, String title, String artist, String genre, String pdfSheetPath, String audioPath, String youtubeUrl, String coverPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.pdfSheetPath = pdfSheetPath;
        this.audioPath = audioPath;
        this.youtubeUrl = youtubeUrl;
        this.coverPath = coverPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getPdfSheetPath() { return pdfSheetPath; }
    public void setPdfSheetPath(String pdfSheetPath) { this.pdfSheetPath = pdfSheetPath; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}