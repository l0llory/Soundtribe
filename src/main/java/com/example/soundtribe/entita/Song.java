package com.example.soundtribe.entita;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private String pdfSheetPath;
    private String audioPath;
    private String youtubeUrl;
    private String coverPath;

    private int uploader_id;      // ID dell'utente che carica
    private String uploaderName; // Nome di chi carica (cache per visualizzazione veloce)
    private String uploaderSurname; // Cognome di chi carica
    private String description;  // Descrizione testuale del brano

    public Song() {}

    public Song(int id, String title, String artist, String genre, String pdfSheetPath,
                String audioPath, String youtubeUrl, String coverPath,
                int uploader_id, String uploaderName, String uploaderSurname, String description) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.pdfSheetPath = pdfSheetPath;
        this.audioPath = audioPath;
        this.youtubeUrl = youtubeUrl;
        this.coverPath = coverPath;
        this.uploader_id = uploader_id;
        this.uploaderName = uploaderName;
        this.uploaderSurname = uploaderSurname;
        this.description = description;
    }

    // Getters e Setters standard
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

    // Nuovi Getters e Setters
    public int getUploader_id() { return uploader_id; }
    public void setUploader_id(int uploader_id) { this.uploader_id = uploader_id; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public String getUploaderSurname() { return uploaderSurname; }
    public void setUploaderSurname(String uploaderSurname) { this.uploaderSurname = uploaderSurname; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}