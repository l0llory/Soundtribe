package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SearchController {

    @FXML
    public Button precP_Search;
    @FXML
    public Button nextP_Search;
    @FXML
    public Button backHome_Search;
    @FXML
    public Button Exit_Search;

    public void initialize(){

        NavigationManager.updateNavigationButtons(precP_Search, nextP_Search);
        NavigationManager.navBack(precP_Search);
        NavigationManager.navForward(nextP_Search);
        NavigationManager.home(backHome_Search);
        NavigationManager.exit(Exit_Search);


    }


}
