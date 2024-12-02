module com.example.ia {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.prefs;

    opens com.example.ia to javafx.fxml;
    opens com.example.ia.model to com.google.gson; // Add this line

    exports com.example.ia;
    exports com.example.ia.model;
}