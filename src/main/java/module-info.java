module com.example.soundtribe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.soundtribe to javafx.fxml;
    exports com.example.soundtribe;
}