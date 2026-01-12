package com.example.soundtribe;


import java.util.Stack;

// Classe che gestisce la pila di memoria che serve per accedere eventualmente al nome della pagin

public class NavigationManager {
    private static Stack<String> backStack = new Stack<>(); // Cronologia passata
    private static Stack<String> forwardStack = new Stack<>(); // Cronologia futura (per il "avanti")

    // Chiama questo ogni volta che l'utente clicca un tasto del menu
    public static void navigateTo(String fxml) {
        backStack.push(fxml);
        forwardStack.clear(); // Se navigo in una nuova pagina, non posso più andare "avanti"
    }

    //Metodo che ti restituisce il nome della classe precedente in cui sei stato
    public static String goBack() {
        if (backStack.size() > 1) {
            forwardStack.push(backStack.pop()); // Sposta la corrente in "avanti"
            return backStack.peek(); // Restituisce la precedente
        }
        return null;
    }

    public static boolean canGoBack(){
        return !backStack.isEmpty();
    }

    public static boolean canGoForeward(){
        return !forwardStack.isEmpty();
    }
}