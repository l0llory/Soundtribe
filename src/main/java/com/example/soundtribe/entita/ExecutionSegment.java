package com.example.soundtribe.entita;

public class ExecutionSegment {
    private int id;
    private int executionId;
    private int userId;
    private String startTime;
    private String endTime;
    private String notes;

    // Costruttore vuoto
    public ExecutionSegment() {}

    // Costruttore completo
    public ExecutionSegment(int id, int executionId, int userId, String startTime, String endTime, String notes) {
        this.id = id;
        this.executionId = executionId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    // --- GETTER E SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExecutionId() { return executionId; }
    public void setExecutionId(int executionId) { this.executionId = executionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}