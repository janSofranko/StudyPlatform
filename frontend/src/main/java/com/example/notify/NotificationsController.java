package com.example.notify;

import com.example.api.ApiClient;
import com.example.notify.NotificationStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class NotificationsController {

    @FXML private ListView<String> listView;

    private final ObjectMapper mapper = new ObjectMapper();
    private Long userId;

    public void setUserId(Long userId) {
        this.userId = userId;
        listView.setItems(NotificationStore.getMessages());
        loadExisting();
    }

    private void loadExisting() {
        try {
            var resp = ApiClient.get("/notifications/" + userId);
            if (resp.statusCode() == 200) {
                JsonNode arr = mapper.readTree(resp.body());
                // vyčistiť, aby sa nezdupľovali
                NotificationStore.getMessages().clear();
                for (JsonNode n : arr) {
                    NotificationStore.getMessages().add(n.get("message").asText());
                }
            }
        } catch (Exception ignored) {}
    }

    @FXML
    private void markRead() {
        try {
            var resp = ApiClient.post("/notifications/" + userId + "/mark-read",
                    mapper.createObjectNode());
            if (resp.statusCode() == 200) {
                NotificationStore.markRead();
            }
        } catch (Exception ignored) {}
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) listView.getScene().getWindow();
        stage.close();
    }
}
