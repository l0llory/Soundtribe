package com.example.soundtribe.entita;

import java.util.Objects;

public class User {

    private int id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private boolean isAdmin;
    private String status; // Sostituito boolean con String ("Pending", "Verified", "Banned")
    private String bannedCommentsCount;

    // Dati opzionali profilo
    private String profilePicPath;
    private String favoriteGenre;

    // NUOVO CAMPO: Motivazione iscrizione
    private String motivation;

    // 1. Costruttore Vuoto
    public User() {
    }

    // 2. Costruttore per Registrazione (Senza ID, status di default a "Pending")
    public User(String name, String surname, String email, String password, String favoriteGenre) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.favoriteGenre = favoriteGenre;
        this.isAdmin = false;
        this.status = "Pending"; // Stato iniziale di default
        this.motivation = "";
    }

    // 3. Costruttore Completo (Usato dal DAO quando legge dal DB)
    public User(int id, String name, String surname, String email, String password,
                boolean isAdmin, String status, String profilePicPath, String favoriteGenre, String motivation) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.status = status;
        this.profilePicPath = profilePicPath;
        this.favoriteGenre = favoriteGenre;
        this.motivation = motivation;
    }

    // --- GETTERS E SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    // --- METODI STANDARD (Override) ---

    @Override
    public String toString() {
        return name + " " + surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id || Objects.equals(email, user.email);
    }

    public void setBannedCommentsCount(String bannedCount) {
        this.bannedCommentsCount = bannedCount;
    }

    public String getBannedCommentsCount() {
        return (this.bannedCommentsCount != null) ? this.bannedCommentsCount : "0";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}

