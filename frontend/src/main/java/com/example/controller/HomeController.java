package com.example.controller;

import com.example.api.ApiClient;
import com.example.store.CurrentUserStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomeController {

    @FXML private Label groupsCount;
    @FXML private Label tasksCount;
    @FXML private Label inProgressCount;
    @FXML private Label doneCount;   // 🔹 nový label pre hotové úlohy

    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;

    private String currentUserName; // nastaví sa z AppController
    private final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public void initialize() {
        loadDashboardData();
        loadChatHistory();
    }


    public void setUserContext(String username) {
        this.currentUserName = username;
    }

    @FXML
    private void handleSend() {
        String text = chatInput.getText();
        if (!text.isEmpty()) {
            try {
                var node = mapper.createObjectNode();
                node.put("fromUser", currentUserName);
                node.put("content", text);
                node.put("timestamp", LocalDateTime.now().format(formatter));

                var response = ApiClient.post("/chat/send", node);
                if (response.statusCode() == 200) {
                    chatList.getItems().add("[" + LocalDateTime.now().format(formatter) + "] "
                            + currentUserName + ": " + text);
                    chatInput.clear();
                } else {
                    System.out.println("Chyba pri odosielaní správy: " + response.body());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadDashboardData() {
        try {
            Long userId = CurrentUserStore.getUserId();

            int groups = 0;
            int tasks = 0;
            int inProgress = 0;
            int done = 0;

            var groupsResp = ApiClient.get("/groups/user/" + userId);
            if (groupsResp.statusCode() == 200) {
                JsonNode groupsArr = mapper.readTree(groupsResp.body());
                groups = groupsArr.size();

                for (JsonNode g : groupsArr) {
                    Long groupId = g.get("id").asLong();

                    var statsResp = ApiClient.get("/tasks/group/" + groupId + "/stats");
                    if (statsResp.statusCode() == 200) {
                        JsonNode stats = mapper.readTree(statsResp.body());
                        tasks += stats.get("total").asInt();
                        inProgress += stats.get("inProgress").asInt();
                        done += stats.get("done").asInt();
                    }
                }
            }

            groupsCount.setText(String.valueOf(groups));
            tasksCount.setText(String.valueOf(tasks));
            inProgressCount.setText(String.valueOf(inProgress));
            doneCount.setText(String.valueOf(done));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory() {
        try {
            var response = ApiClient.get("/chat/history");
            if (response.statusCode() == 200) {
                JsonNode arr = mapper.readTree(response.body());
                for (JsonNode msg : arr) {
                    String from = msg.get("fromUser").asText();
                    String content = msg.get("content").asText();

                    String timestampRaw = msg.has("timestamp") ? msg.get("timestamp").asText() : null;
                    String formattedTime = "??:??";

                    if (timestampRaw != null) {
                        try {
                            LocalDateTime time = LocalDateTime.parse(timestampRaw);
                            formattedTime = time.format(formatter); // HH:mm
                        } catch (Exception ignored) {}
                    }

                    chatList.getItems().add("[" + formattedTime + "] " + from + ": " + content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
