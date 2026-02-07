package com.example.soundtribe;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private boolean isAdmin;

    // NUOVO CAMPO: Ultima ricerca effettuata
    private String lastSearchQuery;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setUser(int userId, boolean isAdmin) {
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    public int getUserId() { return userId; }
    public boolean isAdmin() { return isAdmin; }

    // GETTER E SETTER PER LA RICERCA
    public String getLastSearchQuery() { return lastSearchQuery; }
    public void setLastSearchQuery(String lastSearchQuery) { this.lastSearchQuery = lastSearchQuery; }

    public void cleanUserSession() {
        userId = 0;
        isAdmin = false;
        lastSearchQuery = null; // Puliamo anche la ricerca
    }
}