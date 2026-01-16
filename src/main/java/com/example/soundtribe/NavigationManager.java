package com.example.soundtribe;

import javafx.scene.control.Button;
import java.util.Stack;

public class NavigationManager {
    private static Stack<String> backStack = new Stack<>();
    private static Stack<String> forwardStack = new Stack<>();

    public static void navigateTo(String fxml) {

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

    public static void updateNavigationButtons(Button p, Button n) {
        p.setDisable(!NavigationManager.canGoBack());
        n.setDisable(!NavigationManager.canGoForward());
    }

    public static void navBack(Button button) {
        button.setOnAction(event -> {
            String prevScene = goBack();
            if(prevScene != null) {
                SceneManager.changeScene(event, prevScene, 800, 600, false);
            }
        });
    }

    public static void navForward(Button button){
        // Lo disabilito se non si può andare avanti
        button.setDisable(!canGoForward());

        button.setOnAction(event ->{
            String nextScene = goForward();
            if(nextScene != null){
                SceneManager.changeScene(event, nextScene, 800, 600, false);
            }
        });
    }

    public static void exit(Button button){
        button.setOnAction(event -> {

            UserSession.getInstance().cleanUserSession();
            backStack.clear();
            forwardStack.clear();
            SceneManager.changeScene(event, "Autenticazione.fxml", 600, 500, false);
        });
    }

    public static void home(Button button){
        button.setOnAction(event -> {
            SceneManager.changeScene(event, "Home.fxml", 800, 600, true);
        });
    }
}