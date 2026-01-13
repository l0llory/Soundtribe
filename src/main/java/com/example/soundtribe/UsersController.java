package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class UsersController {

    @FXML
    public Button precP2;
    @FXML
    public Button nextP2;
    @FXML
    public Button backHome1;
    @FXML
    public Button Exit2;

    public void initialize(){

        NavigationManager.updateNavigationButtons(precP2, nextP2);
        NavigationManager.navBack(precP2);
        NavigationManager.navForward(nextP2);
        NavigationManager.home(backHome1);
        NavigationManager.exit(Exit2);
    }


}
