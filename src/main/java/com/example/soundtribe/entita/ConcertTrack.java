package com.example.soundtribe.entita;


public class ConcertTrack {
    private String title;
    private String executors;
    private String startTime;
    private String endTime;
    private String instruments;

    public ConcertTrack(String title, String executors, String startTime, String endTime, String instruments) {
        this.title = title;
        this.executors = executors;
        this.startTime = startTime;
        this.endTime = endTime;
        this.instruments = instruments;
    }

    // Questi metodi DEVONO essere public per essere accessibili dal DAO
    public String getTitle() { return title; }
    public String getExecutors() { return executors; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getInstruments() { return instruments; }
}

