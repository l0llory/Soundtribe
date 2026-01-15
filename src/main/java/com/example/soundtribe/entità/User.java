package com.example.soundtribe.entità;

import java.util.Objects;

public class User {

    private int id; // NUOVO CAMPO FONDAMENTALE PER IL DATABASE
    private String name;
    private String surname;
    private String email;
    private String password;
    private boolean isAdmin;
    private String profilePicPath;
    private String favoriteGenre;


    public User() {}

    // Costruttore per la creazione (senza ID, lo decide il DB)
    public User(String name, String surname, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isAdmin = false;
    }

    // Costruttore completo (quando leggi dal DB)
    public User(int id, String name, String surname, String email, String password, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // Getters e Setters
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

    public String getProfilePicPath() { return profilePicPath; }
    public void setProfilePicPath(String profilePicPath) { this.profilePicPath = profilePicPath; }

    public String getFavoriteGenre() { return favoriteGenre; }
    public void setFavoriteGenre(String favoriteGenre) { this.favoriteGenre = favoriteGenre; }

    @Override
    public String toString() {
        return name + " " + surname;
    }

    public boolean equal(User other){
        return Objects.equals(this.email, other.getEmail());
    }
}