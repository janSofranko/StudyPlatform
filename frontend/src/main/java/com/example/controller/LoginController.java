package com.example.controller;

import com.example.api.ApiClient;
import com.example.notify.NotificationStore;
import com.example.store.CurrentUserStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Label invalidLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private void handleLogin() {
        try {
            var response = ApiClient.post("/users/login",
                    mapper.createObjectNode()
                            .put("name", usernameField.getText())
                            .put("password", passwordField.getText()));

            if (response.statusCode() == 200) {

                JsonNode json = mapper.readTree(response.body());
                Long userId = json.get("id").asLong();
                String userName = json.get("name").asText();
                String userEmail = json.get("email").asText();

                // store globally
                CurrentUserStore.set(userId, userName, userEmail);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app1.fxml"));
                AnchorPane root = loader.load();

                AppController appController = loader.getController();
                appController.initUser(userName, userId, userEmail);

                try {
                    var unreadResp = ApiClient.get("/notifications/" + userId + "/unread-count");
                    if (unreadResp.statusCode() == 200) {
                        long unread = mapper.readTree(unreadResp.body()).asLong();
                        if (unread > 0) NotificationStore.setUnread(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));

            } else {
                JsonNode json = mapper.readTree(response.body());

                if (json.has("message")) {
                    invalidLabel.setText(json.get("message").asText());
                } else {
                    invalidLabel.setText("Login failed.");
                }

                invalidLabel.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            invalidLabel.setText("Connection error.");
            invalidLabel.setVisible(true);
        }
    }

    @FXML
    private void openRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            AnchorPane root = loader.load();


            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrácia");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
