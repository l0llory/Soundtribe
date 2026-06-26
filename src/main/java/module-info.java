module com.example.soundtribe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.zaxxer.hikari;


    opens com.example.soundtribe to javafx.fxml;
    exports com.example.soundtribe;
    exports com.example.soundtribe.entita;
    opens com.example.soundtribe.entita to javafx.fxml;
    exports com.example.soundtribe.controller;
    opens com.example.soundtribe.controller to javafx.fxml;
    exports com.example.soundtribe.manager;
    opens com.example.soundtribe.manager to javafx.fxml;
    exports com.example.soundtribe.item;
    opens com.example.soundtribe.item to javafx.fxml;
    exports com.example.soundtribe.dao;
    opens com.example.soundtribe.dao to javafx.fxml;
}