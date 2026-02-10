package com.example.soundtribe;

import com.example.soundtribe.entità.Esecution;
import com.example.soundtribe.entità.Song;

public class MusicItem {
    private Song song;
    private Esecution execution;
    private boolean isSong;

    public MusicItem(Song song) {
        this.song = song;
        this.isSong = true;
    }

    public MusicItem(Esecution execution) {
        this.execution = execution;
        this.isSong = false;
    }

    public boolean isSong() { return isSong; }
    public Song getSong() { return song; }
    public Esecution getExecution() { return execution; }

    // Helper per il titolo (usato per ordinamento/filtro)
    public String getTitle() {
        return isSong ? song.getTitle() : execution.getTitle();
    }

    // Helper per l'artista/esecutore
    public String getArtist() {
        return isSong ? song.getArtist() : execution.getExecutors();
    }

    // Helper per il genere (le esecuzioni non hanno genere, usiamo stringa vuota o "Varie")
    public String getGenre() {
        return isSong ? song.getGenre() : "Esecuzione";
    }
}