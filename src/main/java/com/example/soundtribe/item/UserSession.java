package com.example.soundtribe.item;

import com.example.soundtribe.entita.User;

public class UserSession {
    private static volatile UserSession instance;
    private int userId;
    public boolean isAdmin;
    private User loggedUser;
    private String lastSearchQuery;
    private String lastUserSearchQuery;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) instance = new UserSession();
            }
        }
        return instance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    // Metodo aggiornato per salvare anche l'oggetto User
    public void setUser(User user) {
        if (user != null) {
            this.userId = user.getId();
            this.isAdmin = user.isAdmin();
            this.loggedUser = user;
        }
    }

    public void cleanUserSession() {
        userId = 0;
        isAdmin = false;
        loggedUser = null;
        lastSearchQuery = null;
        lastUserSearchQuery = null;
    }

    public int getUserId() {
        return userId;

    }

    public void setLastSearchQuery(String query) {
        this.lastSearchQuery = query;

    }

    public String getLastSearchQuery() {
        return lastSearchQuery;
    }

    public void setLastUserSearchQuery(String query) {
        this.lastUserSearchQuery = query;
    }

    public String getLastUserSearchQuery() {
        return lastUserSearchQuery;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

}