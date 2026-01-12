package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SongsController {

    @FXML
    public Button precP_Songs;
    @FXML
    public Button nextP_Songs;
    @FXML
    public Button backHome_Songs;
    @FXML
    public Button Exit_Songs;

    public void initialize(){

        NavigationManager.updateNavigationButtons(precP_Songs, nextP_Songs);
        NavigationManager.navBack(precP_Songs);
        NavigationManager.navForward(nextP_Songs);
        NavigationManager.home(backHome_Songs);
        NavigationManager.exit(Exit_Songs);

    }
}
