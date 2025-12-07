package com.example.controller;

import com.example.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddMemberController {

    @FXML private ListView<String> usersList;
    @FXML private Label errorLabel;

    private Long groupId;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, Long> userMap = new HashMap<>();

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
        loadUsers();
    }

    private void loadUsers() {
        try {
            var response = ApiClient.get("/users");
            if (response.statusCode() == 200) {
                JsonNode arr = mapper.readTree(response.body());
                usersList.getItems().clear();
                userMap.clear();
                for (JsonNode u : arr) {
                    String display = u.get("name").asText() + " (" + u.get("email").asText() + ")";
                    Long id = u.get("id").asLong();
                    usersList.getItems().add(display);
                    userMap.put(display, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Nepodarilo sa načítať používateľov.");
        }
    }

    @FXML
    private void handleAdd() {
        String selected = usersList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Long userId = userMap.get(selected);
        try {
            var response = ApiClient.post("/memberships/join?userId=" + userId + "&groupId=" + groupId, new HashMap<>());
            if (response.statusCode() == 200) {
                closeWindow();
            } else {

                JsonNode json = mapper.readTree(response.body());
                if (json.has("message")) {
                    showError(json.get("message").asText());
                } else {
                    showError("Chyba: " + response.body());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Server error.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) usersList.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
