package com.example.soundtribe.entità;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Execution {
    private int id;
    private int songId;
    private String title;
    private String filePath;
    private String fileType;
    private String executors;
    private String instruments; // Manteniamo String per compatibilità DB (es. "Chitarra, Voce")
    private String duration;
    private boolean isLive;
    private Date recordingDate;
    private String recordingPlace;
    private boolean isConcert;
    private boolean isSelfPerformer;
    private int uploaderId;
    private List<ExecutionSegment> segments;

    public Execution() {}

    public Execution(int id, int songId, String title, String filePath, String fileType, String executors,
                     String instruments, String duration, boolean isLive, Date recordingDate,
                     String recordingPlace, boolean isConcert, boolean isSelfPerformer, int uploaderId) {
        this.id = id;
        this.songId = songId;
        this.title = title;
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
        this.uploaderId = uploaderId;
        this.segments = new ArrayList<>();
    }

    // Metodo helper per ottenere l'oggetto Instrument (se è singolo)
    public Instrument getInstrumentObject() {
        return new Instrument(this.instruments);
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSongId() { return songId; }
    public void setSongId(int songId) { this.songId = songId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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

    public int getUploaderId() { return uploaderId; }
    public void setUploaderId(int uploaderId) { this.uploaderId = uploaderId; }

    public List<ExecutionSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<ExecutionSegment> segments) {
        this.segments = segments;
    }

    public void addSegment(ExecutionSegment segment) {
        if (this.segments == null) {
            this.segments = new ArrayList<>();
        }
        this.segments.add(segment);
    }
}