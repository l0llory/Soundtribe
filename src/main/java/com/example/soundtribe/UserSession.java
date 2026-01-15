package com.example.soundtribe;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private boolean isAdmin;

    // Costruttore privato: nessuno può creare istanze con 'new' dall'esterno
    private UserSession() {
        this.userId = 0; // 0 significa nessun utente loggato
        this.isAdmin = false;
    }

    // Metodo statico per ottenere l'unica istanza disponibile
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // GETTERS & SETTERS
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    // Metodo utile per il Logout
    public void cleanUserSession() {
        userId = 0;
        isAdmin = false;
    }
}