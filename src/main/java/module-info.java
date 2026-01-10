module com.example.soundtribe {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.soundtribe to javafx.fxml;
    exports com.example.soundtribe;
}