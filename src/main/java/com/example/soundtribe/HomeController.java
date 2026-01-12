package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class HomeController {
    @FXML
    public Button precP;
    @FXML
    public Button nextP;
    @FXML
    public Button handleBraniMusicali;
    @FXML
    public Button handleUtenti;
    @FXML
    public Button handleCommentiNote;
    @FXML
    public Button handleRicerca;
    @FXML
    public Button handleCaricaMateriale;
    @FXML
    public Button handleAmministrazione;

    public void initialize(){
        // OGNI VOLTA CHE SI CAMBIA SCENA RICORDARSI DI CHIAMARE IL METODO: NavigationManager.navigateTo("nome file fxml in cui mi sto spostando")
        updateNavigationButtons();
        precP.setOnAction(event -> {
            String prevScene = NavigationManager.goBack();
            if(prevScene != null){
                SceneManager.changeScene(event, prevScene, 800, 500, false);
            }
        });
        nextP.setOnAction(event -> {
            String nextScene = NavigationManager.goBack();
            if(nextScene != null){
                SceneManager.changeScene(event, nextScene, 800, 600, false);
            }
        });
    }

    private void updateNavigationButtons() {

        precP.setDisable(!NavigationManager.canGoBack());
        nextP.setDisable(!NavigationManager.canGoForeward());
    }
}
