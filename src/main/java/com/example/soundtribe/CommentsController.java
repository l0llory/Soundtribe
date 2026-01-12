package com.example.soundtribe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CommentsController {
    @FXML
    public Button precP_Comments;
    @FXML
    public Button nextP_Comments;
    @FXML
    public Button backHome_Comments;
    @FXML
    public Button Exit_Comments;

    public void initialize(){

        NavigationManager.updateNavigationButtons(precP_Comments, nextP_Comments);
        NavigationManager.navBack(precP_Comments);
        NavigationManager.navForward(nextP_Comments);
        NavigationManager.home(backHome_Comments);
        NavigationManager.exit(Exit_Comments);


    }
}
