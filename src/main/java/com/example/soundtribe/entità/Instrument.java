package com.example.soundtribe.entità;

import java.util.Objects;

public class Instrument {
    private String name;

    public Instrument(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Fondamentale per visualizzare il nome nella ComboBox
    @Override
    public String toString() {
        return name;
    }

    // Utile per confrontare gli oggetti nelle liste
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instrument that = (Instrument) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}