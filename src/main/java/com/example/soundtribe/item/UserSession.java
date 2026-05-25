package com.example.soundtribe.item;

import com.example.soundtribe.entità.User;

public class UserSession {
    private static volatile UserSession instance;
    private int userId;
    public boolean isAdmin;
    private User loggedUser;

    private String lastSearchQuery;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (UserSession.class) { // Double-checked locking per thread-safety
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

    // Vecchio metodo mantenuto per retrocompatibilità
    public void setUser(int userId, boolean isAdmin) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        if (this.loggedUser == null) { // Se non è ancora stato impostato un oggetto User completo
            this.loggedUser = new User(); // Creiamo un oggetto User "wrapper" temporaneo
        }
        // Aggiorniamo l'ID e lo stato di admin dell'oggetto User esistente o appena creato
        this.loggedUser.setId(userId);
        this.loggedUser.setAdmin(isAdmin); // This line was already there, but the previous diffs were a bit messy.
    }

    public void cleanUserSession() {
        userId = 0;
        isAdmin = false;
        loggedUser = null;
        lastSearchQuery = null; // Puliamo anche la ricerca
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

    public User getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(User loggedUser) { this.loggedUser = loggedUser; }

    public boolean isAdmin() {
        return isAdmin;
    }

}