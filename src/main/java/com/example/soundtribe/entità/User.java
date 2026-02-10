package com.example.soundtribe.entità;

import java.util.Objects;

public class User {

    private int id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private boolean isAdmin;
    private boolean isApproved; // Campo per gestire "In Attesa" vs "Attivo"

    // Dati opzionali profilo
    private String profilePicPath;
    private String favoriteGenre;

    // 1. Costruttore Vuoto (Necessario per alcune operazioni)
    public User() {}

    // 2. Costruttore per Registrazione (Senza ID, perché lo genera il DB)
    public User(String name, String surname, String email, String password, String favoriteGenre) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.favoriteGenre = favoriteGenre;
        this.isAdmin = false;
        this.isApproved = false;
    }

    // 3. Costruttore Completo (Usato dal DAO quando legge dal DB)
    public User(int id, String name, String surname, String email, String password,
                boolean isAdmin, boolean isApproved, String profilePicPath, String favoriteGenre) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isApproved = isApproved;
        this.profilePicPath = profilePicPath;
        this.favoriteGenre = favoriteGenre;
    }

    // --- GETTERS E SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    // Fondamentali per la gestione richieste
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public String getProfilePicPath() { return profilePicPath; }
    public void setProfilePicPath(String profilePicPath) { this.profilePicPath = profilePicPath; }

    public String getFavoriteGenre() { return favoriteGenre; }
    public void setFavoriteGenre(String favoriteGenre) { this.favoriteGenre = favoriteGenre; }

    // --- METODI STANDARD (Override) ---

    @Override
    public String toString() {
        return name + " " + surname;
    }

    // Override corretto di equals per confrontare gli utenti (basato su ID e Email)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id || Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

}