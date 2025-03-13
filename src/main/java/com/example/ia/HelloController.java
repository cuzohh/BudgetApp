package com.example.ia;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText; // Label to display the welcome text.

    @FXML
    protected void onHelloButtonClick() { // Called when the button is clicked.
        welcomeText.setText("Hello, World!");
    }
}