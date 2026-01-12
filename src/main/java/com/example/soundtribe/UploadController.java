package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class UploadController {
    @FXML
    public Button precP_Upload;
    @FXML
    public Button nextP_Upload;
    @FXML
    public Button backHome_Upload;
    @FXML
    public Button Exit_Upload;

    public void initialize(){

        NavigationManager.updateNavigationButtons(precP_Upload, nextP_Upload);
        NavigationManager.navBack(precP_Upload);
        NavigationManager.navForward(nextP_Upload);
        NavigationManager.home(backHome_Upload);
        NavigationManager.exit(Exit_Upload);


    }
}
