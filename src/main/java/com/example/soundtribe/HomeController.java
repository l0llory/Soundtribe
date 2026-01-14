package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

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
    @FXML
    public Button Exit;

    public void initialize(){
        // OGNI VOLTA CHE SI CAMBIA SCENA RICORDARSI DI CHIAMARE IL METODO: NavigationManager.navigateTo("nome file fxml in cui mi sto spostando")
        NavigationManager.updateNavigationButtons(precP, nextP);

        NavigationManager.navBack(precP);

        NavigationManager.exit(Exit);

        handleBraniMusicali.setOnAction(event -> {
            SceneManager.changeScene(event, "braniMusicali.fxml", 800, 600, true);
        });
        handleUtenti.setOnAction(event -> {
            SceneManager.changeScene(event, "gestioneUtenti.fxml", 800, 600, true);

        });
        handleRicerca.setOnAction(event -> {
            SceneManager.changeScene(event, "Ricerca.fxml", 800, 600, true);

        });
        handleCommentiNote.setOnAction(event -> {
            SceneManager.changeScene(event, "commentiENote.fxml", 800, 600, true);
        });
        handleCaricaMateriale.setOnAction(event -> {
            SceneManager.changeScene(event, "caricaMateriale.fxml", 800, 600, true);

        });
        handleAmministrazione.setOnAction(event -> {
            SceneManager.changeScene(event, "Amministrazione.fxml", 800, 600, true);
        });

    }

}
