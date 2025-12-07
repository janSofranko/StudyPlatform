package com.example.controller;

import com.example.api.ApiClient;
import com.example.store.CurrentUserStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

public class TasksController {

    @FXML private ListView<String> groupsList;
    @FXML private TableView<Map<String, Object>> tasksTable;
    @FXML private TableColumn<Map<String, Object>, String> colName;
    @FXML private TableColumn<Map<String, Object>, String> colStatus;
    @FXML private ListView<String> assigneesList;

    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, Long> groupMap = new HashMap<>();
    private Long selectedGroupId = null;
    private Long selectedTaskId = null;

    @FXML
    public void initialize() {
        loadGroups();

        groupsList.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            if (now != null) {
                selectedGroupId = groupMap.get(now);
                loadTasks(selectedGroupId);
            }
        });

        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            assigneesList.getItems().clear();
            if (now != null && now.containsKey("id")) {
                selectedTaskId = (Long) now.get("id");
                loadAssignees(selectedTaskId);
            } else {
                selectedTaskId = null;
            }
        });

        colName.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(() ->
                (String) data.getValue().getOrDefault("title", "")));
        colStatus.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(() ->
                (String) data.getValue().getOrDefault("status", "")));
    }

    private void loadGroups() {
        try {
            Long userId = CurrentUserStore.getUserId();
            var resp = ApiClient.get("/groups/user/" + userId);
            if (resp.statusCode() == 200) {
                groupsList.getItems().clear();
                groupMap.clear();
                JsonNode arr = mapper.readTree(resp.body());
                for (JsonNode g : arr) {
                    String display = g.get("name").asText();
                    Long id = g.get("id").asLong();
                    groupsList.getItems().add(display);
                    groupMap.put(display, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasks(Long groupId) {
        try {
            var resp = ApiClient.get("/tasks/group/" + groupId);
            if (resp.statusCode() == 200) {
                tasksTable.getItems().clear();
                JsonNode arr = mapper.readTree(resp.body());

                List<Map<String,Object>> rows = new ArrayList<>();
                for (JsonNode t : arr) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", t.get("id").asLong());
                    row.put("title", t.get("title").asText());
                    row.put("status", t.get("status").asText());
                    rows.add(row);
                }


                rows.sort(Comparator.comparing(r -> {
                    String status = (String) r.get("status");
                    return switch (status) {
                        case "OPEN" -> 0;
                        case "IN_PROGRESS" -> 1;
                        case "DONE" -> 2;
                        default -> 3;
                    };
                }));

                tasksTable.getItems().addAll(rows);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAssignees(Long taskId) {
        try {
            var resp = ApiClient.get("/tasks/" + taskId + "/assignees");
            if (resp.statusCode() == 200) {
                assigneesList.getItems().clear();
                JsonNode arr = mapper.readTree(resp.body());
                for (JsonNode a : arr) {
                    String name = a.get("user").get("name").asText();
                    String email = a.get("user").get("email").asText();
                    String role = a.get("role").asText();
                    assigneesList.getItems().add(name + " (" + email + ") [" + role + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTask() {
        if (selectedGroupId == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Názov novej úlohy");
        var result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String title = result.get();

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setHeaderText("Popis úlohy (voliteľné)");
        String description = descDialog.showAndWait().orElse("");

        try {
            var body = new HashMap<String, Object>();
            body.put("title", title);
            body.put("description", description);
            body.put("status", "OPEN");
            body.put("groupId", selectedGroupId);
            body.put("createdBy", CurrentUserStore.getUserId());

            var resp = ApiClient.post("/tasks", body);
            if (resp.statusCode() == 200) {
                loadTasks(selectedGroupId);
            } else {
                showAlert("Chyba", resp.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAssignUser() {
        if (selectedTaskId == null) return;

        try {
            var resp = ApiClient.get("/users");
            if (resp.statusCode() == 200) {
                JsonNode arr = mapper.readTree(resp.body());
                ChoiceDialog<String> dialog = new ChoiceDialog<>();
                dialog.setHeaderText("Vyber používateľa");
                for (JsonNode u : arr) {
                    dialog.getItems().add(u.get("name").asText() + " (" + u.get("email").asText() + ")");
                }
                var chosen = dialog.showAndWait();
                if (chosen.isPresent()) {
                    String display = chosen.get();
                    Long userId = null;
                    for (JsonNode u : arr) {
                        String d = u.get("name").asText() + " (" + u.get("email").asText() + ")";
                        if (d.equals(display)) {
                            userId = u.get("id").asLong();
                            break;
                        }
                    }
                    if (userId != null) {
                        var body = mapper.createObjectNode()
                                .put("userId", userId)
                                .put("role", "assignee");
                        var assignResp = ApiClient.post("/tasks/" + selectedTaskId + "/assign", body);
                        if (assignResp.statusCode() == 200) {
                            loadAssignees(selectedTaskId);
                            loadTasks(selectedGroupId); // refresh status
                        } else {
                            showAlert("Chyba", assignResp.body());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Chyba", "Server error.");
        }
    }

    @FXML
    private void handleUnassignUser() {
        if (selectedTaskId == null) return;
        String selectedUser = assigneesList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        try {
            var resp = ApiClient.get("/tasks/" + selectedTaskId + "/assignees");
            if (resp.statusCode() == 200) {
                JsonNode arr = mapper.readTree(resp.body());
                Long userId = null;
                for (JsonNode a : arr) {
                    String name = a.get("user").get("name").asText();
                    String email = a.get("user").get("email").asText();
                    String role = a.get("role").asText();
                    String label = name + " (" + email + ") [" + role + "]";
                    if (label.equals(selectedUser)) {
                        userId = a.get("user").get("id").asLong();
                        break;
                    }
                }
                if (userId != null) {
                    var unResp = ApiClient.post("/tasks/" + selectedTaskId + "/unassign/" + userId, mapper.createObjectNode());
                    if (unResp.statusCode() == 200) {
                        loadAssignees(selectedTaskId);
                    } else {
                        showAlert("Chyba", unResp.body());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Chyba", "Server error.");
        }
    }


    @FXML
    private void handleMarkDone() {
        if (selectedTaskId == null) return;

        try {
            var body = mapper.createObjectNode().put("status", "DONE");
            var resp = ApiClient.post("/tasks/" + selectedTaskId + "/status", body);
            if (resp.statusCode() == 200) {
                loadTasks(selectedGroupId);
            } else {
                showAlert("Chyba", resp.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Chyba", "Server error.");
        }
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
