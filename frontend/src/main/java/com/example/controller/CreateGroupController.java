package com.example.controller;

import com.example.api.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;

public class CreateGroupController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;

    private Long currentUserId = 1L; // nastav aktuálneho používateľa

    @FXML
    private void handleCreate() {
        try {
            var body = new HashMap<String, Object>();
            body.put("name", nameField.getText());
            body.put("description", descField.getText());
            body.put("creatorId", currentUserId);

            var response = ApiClient.post("/groups", body);
            if (response.statusCode() == 200) {
                closeWindow();
            } else {
                System.out.println("Chyba: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
