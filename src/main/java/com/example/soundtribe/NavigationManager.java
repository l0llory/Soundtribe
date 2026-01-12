package com.example.soundtribe;

import java.util.Stack;

public class NavigationManager {
    private static Stack<String> backStack = new Stack<>();
    private static Stack<String> forwardStack = new Stack<>();

    public static void navigateTo(String fxml) {
        // Se la pagina è già in cima alla cronologia, evitiamo duplicati
        if (backStack.isEmpty() || !backStack.peek().equals(fxml)) {
            backStack.push(fxml);
            forwardStack.clear();
        }
    }

    public static String goBack() {
        if (backStack.size() > 1) {
            forwardStack.push(backStack.pop());
            return backStack.peek();
        }
        return null;
    }

    public static String goForward() {
        if (!forwardStack.isEmpty()) {
            String nextStage = forwardStack.pop();
            backStack.push(nextStage);
            return nextStage;
        }
        return null;
    }

    public static boolean canGoBack(){
        return backStack.size() > 1;
    }

    public static boolean canGoForward(){
        return !forwardStack.isEmpty();
    }
}