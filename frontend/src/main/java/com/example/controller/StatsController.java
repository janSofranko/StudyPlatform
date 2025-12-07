package com.example.controller;

import com.example.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class StatsController {

    @FXML
    private ComboBox<String> groupSelector;
    @FXML
    private PieChart taskChart;
    @FXML
    private Label totalLabel;
    @FXML
    private Label openLabel;
    @FXML
    private Label inProgressLabel;
    @FXML
    private Label doneLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        System.out.println("✅ StatsController initialized");
        loadGroups();


        groupSelector.setOnAction(e -> {
            String selected = groupSelector.getSelectionModel().getSelectedItem();
            System.out.println("➡️ Vybral si skupinu: " + selected);
            if (selected != null) {
                Long groupId = Long.parseLong(selected.split(" - ")[0]); // formát "id - názov"
                loadStats(groupId);
            }
        });
    }

    private void loadGroups() {
        try {
            var response = ApiClient.get("/groups");

            System.out.println("📡 Odpoveď zo servera (groups): " + response.body());

            if (response.statusCode() == 200) {
                JsonNode arr = mapper.readTree(response.body());
                groupSelector.getItems().clear();
                for (JsonNode g : arr) {
                    Long id = g.get("id").asLong();
                    String name = g.get("name").asText();
                    System.out.println("➕ Pridávam skupinu: " + id + " - " + name);
                    groupSelector.getItems().add(id + " - " + name);
                }
            } else {
                System.err.println("❌ Chybný status kód: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStats(Long groupId) {
        try {
            var response = ApiClient.get("/tasks/group/" + groupId + "/stats");

            System.out.println("📡 Odpoveď zo servera (stats): " + response.body());

            if (response.statusCode() == 200) {
                JsonNode stats = mapper.readTree(response.body());

                long total = stats.has("total") ? stats.get("total").asLong() : 0;
                long open = stats.has("open") ? stats.get("open").asLong() : 0;
                long inProgress = stats.has("inProgress") ? stats.get("inProgress").asLong() : 0;
                long done = stats.has("done") ? stats.get("done").asLong() : 0;

                totalLabel.setText("Spolu: " + total);
                openLabel.setText("Otvorené: " + open);
                inProgressLabel.setText("Rozpracované: " + inProgress);
                doneLabel.setText("Dokončené: " + done);

                taskChart.getData().clear();

                if (total == 0) {
                    taskChart.getData().add(new PieChart.Data("Žiadne dáta", 1));
                } else {
                    taskChart.getData().add(new PieChart.Data("Otvorené", open));
                    taskChart.getData().add(new PieChart.Data("Rozpracované", inProgress));
                    taskChart.getData().add(new PieChart.Data("Dokončené", done));
                }

                taskChart.setLegendVisible(true);
            } else {
                System.err.println(" Chybný status kód: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
