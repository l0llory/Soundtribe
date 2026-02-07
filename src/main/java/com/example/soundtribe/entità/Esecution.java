package com.example.soundtribe.entità;

import java.sql.Date;

public class Esecution {
    private int id;
    private int songId;        // Collegamento alla canzone (Foreign Key)
    private String filePath;   // Dove si trova il file o l'URL
    private String fileType;   // "MP3", "MP4", "PDF", "YouTube", ecc.

    // Metadati specifici richiesti
    private String executors;
    private String instruments;
    private String duration;
    private boolean isLive;
    private Date recordingDate; // Usiamo java.sql.Date per il database
    private String recordingPlace;
    private boolean isConcert;
    private boolean isSelfPerformer; // Se l'utente è l'interprete

    public Esecution() {}

    public Esecution(int id, int songId, String filePath, String fileType, String executors,
                     String instruments, String duration, boolean isLive, Date recordingDate,
                     String recordingPlace, boolean isConcert, boolean isSelfPerformer) {
        this.id = id;
        this.songId = songId;
        this.filePath = filePath;
        this.fileType = fileType;
        this.executors = executors;
        this.instruments = instruments;
        this.duration = duration;
        this.isLive = isLive;
        this.recordingDate = recordingDate;
        this.recordingPlace = recordingPlace;
        this.isConcert = isConcert;
        this.isSelfPerformer = isSelfPerformer;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSongId() { return songId; }
    public void setSongId(int songId) { this.songId = songId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getExecutors() { return executors; }
    public void setExecutors(String executors) { this.executors = executors; }

    public String getInstruments() { return instruments; }
    public void setInstruments(String instruments) { this.instruments = instruments; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public boolean isLive() { return isLive; }
    public void setLive(boolean live) { isLive = live; }

    public Date getRecordingDate() { return recordingDate; }
    public void setRecordingDate(Date recordingDate) { this.recordingDate = recordingDate; }

    public String getRecordingPlace() { return recordingPlace; }
    public void setRecordingPlace(String recordingPlace) { this.recordingPlace = recordingPlace; }

    public boolean isConcert() { return isConcert; }
    public void setConcert(boolean concert) { isConcert = concert; }

    public boolean isSelfPerformer() { return isSelfPerformer; }
    public void setSelfPerformer(boolean selfPerformer) { isSelfPerformer = selfPerformer; }
}