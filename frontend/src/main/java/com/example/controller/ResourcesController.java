package com.example.controller;

import com.example.api.ApiClient;
import com.example.controller.helpMetods.ResourceRow;
import com.example.store.CurrentUserStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;

public class ResourcesController {

    @FXML private ListView<String> groupsList;

    // Úlohy stredná tabuľka
    @FXML private TableView<TaskRow> tasksTable;
    @FXML private TableColumn<TaskRow, String> colTaskTitle;
    @FXML private TableColumn<TaskRow, String> colTaskStatus;

    // Materiály pravá tabuľka
    @FXML private TableView<ResourceRow> resourcesTable;
    @FXML private TableColumn<ResourceRow, String> colResTitle;
    @FXML private TableColumn<ResourceRow, String> colResUploader;
    @FXML private TableColumn<ResourceRow, String> colResLink;

    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, Long> groupMap = new HashMap<>();
    private Long selectedGroupId = null;
    private Long selectedTaskId = null;

    @FXML
    public void initialize() {
        setupColumns();
        loadUserGroups();

        groupsList.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            tasksTable.getItems().clear();
            resourcesTable.getItems().clear();
            selectedTaskId = null;
            if (now != null) {
                selectedGroupId = groupMap.get(now);
                loadTasksForGroup(selectedGroupId);
            }
        });

        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            resourcesTable.getItems().clear();
            if (now != null) {
                selectedTaskId = now.getId();
                loadResourcesForTask(selectedTaskId);
            }
        });


        resourcesTable.setRowFactory(tv -> {
            TableRow<ResourceRow> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openLink(row.getItem().getUrl());
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        // Tasks
        colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colTaskStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        // Resources
        colResTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colResUploader.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUploader()));
        colResLink.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUrl()));
    }

    private void loadUserGroups() {
        try {
            Long userId = CurrentUserStore.getUserId();
            var response = ApiClient.get("/groups/user/" + userId);
            if (response.statusCode() == 200) {
                groupsList.getItems().clear();
                groupMap.clear();
                JsonNode arr = mapper.readTree(response.body());
                for (JsonNode g : arr) {
                    Long id = g.get("id").asLong();
                    String name = g.get("name").asText();
                    groupsList.getItems().add(name);
                    groupMap.put(name, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasksForGroup(Long groupId) {
        try {
            var response = ApiClient.get("/tasks/group/" + groupId);
            if (response.statusCode() == 200) {
                tasksTable.getItems().clear();
                JsonNode arr = mapper.readTree(response.body());
                List<TaskRow> tasks = new ArrayList<>();
                for (JsonNode t : arr) {
                    Long id = t.get("id").asLong();
                    String title = t.get("title").asText();
                    String status = t.get("status").asText();
                    tasks.add(new TaskRow(id, title, status));
                }

                tasks.sort((a, b) -> {
                    int orderA = switch (a.getStatus()) {
                        case "OPEN" -> 0;
                        case "IN_PROGRESS" -> 1;
                        case "DONE" -> 2;
                        default -> 3;
                    };
                    int orderB = switch (b.getStatus()) {
                        case "OPEN" -> 0;
                        case "IN_PROGRESS" -> 1;
                        case "DONE" -> 2;
                        default -> 3;
                    };
                    return Integer.compare(orderA, orderB);
                });
                tasksTable.getItems().addAll(tasks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadResourcesForTask(Long taskId) {
        try {
            var response = ApiClient.get("/resources/task/" + taskId);
            if (response.statusCode() == 200) {
                resourcesTable.getItems().clear();


                System.out.println("Resources JSON: " + response.body());

                JsonNode arr = mapper.readTree(response.body());
                for (JsonNode r : arr) {
                    Long id = r.get("id").asLong();
                    String title = r.get("title").asText();
                    String url = r.get("url").asText();


                    String uploader;
                    if (r.has("uploadedBy") && r.get("uploadedBy").has("name")) {
                        uploader = r.get("uploadedBy").get("name").asText();
                    } else if (r.has("uploadedById")) {
                        uploader = "User#" + r.get("uploadedById").asLong();
                    } else {
                        uploader = "Neznámy";
                    }

                    resourcesTable.getItems().add(new ResourceRow(id, title, url, uploader));
                }
            } else {
                System.out.println("Chyba pri načítaní materiálov: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDeleteResource() {
        ResourceRow selected = resourcesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Najprv vyber materiál.");
            return;
        }

        try {
            var response = ApiClient.delete("/resources/" + selected.getId());
            if (response.statusCode() == 200) {
                // obnov tabuľku po vymazaní
                loadResourcesForTask(selectedTaskId);
            } else {
                System.out.println("Chyba pri mazaní: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddResource() {
        if (selectedTaskId == null) {
            showError("Najprv vyber úlohu.");
            return;
        }

        try {
            TextInputDialog titleDialog = new TextInputDialog();
            titleDialog.setHeaderText("Pridaj nový materiál");
            titleDialog.setContentText("Zadaj názov:");

            Optional<String> titleResult = titleDialog.showAndWait();
            if (titleResult.isEmpty()) return;
            String title = titleResult.get();

            TextInputDialog urlDialog = new TextInputDialog();
            urlDialog.setHeaderText("Pridaj nový materiál");
            urlDialog.setContentText("Zadaj URL:");

            Optional<String> urlResult = urlDialog.showAndWait();
            if (urlResult.isEmpty()) return;
            String url = urlResult.get();

            var body = mapper.createObjectNode();
            body.put("title", title);
            body.put("url", url);
            body.put("groupId", selectedGroupId);
            body.put("uploadedById", CurrentUserStore.getUserId());
            body.put("taskId", selectedTaskId);

            var response = ApiClient.post("/resources", body);
            if (response.statusCode() == 200) {
                loadResourcesForTask(selectedTaskId);
            } else {
                System.out.println("Chyba pri ukladaní: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }



    public static class TaskRow {
        private Long id;
        private String title;
        private String status;

        public TaskRow(Long id, String title, String status) {
            this.id = id;
            this.title = title;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getStatus() { return status; }
    }

}
