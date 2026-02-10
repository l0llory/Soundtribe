package com.example.soundtribe;

import com.example.soundtribe.entità.Concert;
import com.example.soundtribe.entità.Esecution;
import com.example.soundtribe.entità.Song;

public class MusicItem {
    private Song song;
    private Esecution execution;
    private Concert concert; // NUOVO CAMPO

    // Enum per identificare il tipo in modo pulito
    public enum Type { SONG, EXECUTION, CONCERT }
    private final Type type;

    public MusicItem(Song song) {
        this.song = song;
        this.type = Type.SONG;
    }

    public MusicItem(Esecution execution) {
        this.execution = execution;
        this.type = Type.EXECUTION;
    }

    public MusicItem(Concert concert) { // NUOVO COSTRUTTORE
        this.concert = concert;
        this.type = Type.CONCERT;
    }

    public boolean isSong() { return type == Type.SONG; }
    public boolean isExecution() { return type == Type.EXECUTION; }
    public boolean isConcert() { return type == Type.CONCERT; } // NUOVO

    public Song getSong() { return song; }
    public Esecution getExecution() { return execution; }
    public Concert getConcert() { return concert; } // NUOVO

    // Helper unificati per titolo e artista
    public String getTitle() {
        if (isSong()) return song.getTitle();
        if (isExecution()) return execution.getTitle();
        return concert.getTitle(); // Concert
    }

    public String getArtist() {
        if (isSong()) return song.getArtist();
        if (isExecution()) return execution.getExecutors();
        return concert.getArtist(); // Concert
    }
}