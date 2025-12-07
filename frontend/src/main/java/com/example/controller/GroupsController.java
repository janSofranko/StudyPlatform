    package com.example.controller;

    import com.example.api.ApiClient;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Scene;
    import javafx.stage.Stage;
    import javafx.scene.control.*;

    import java.util.HashMap;
    import java.util.Map;

    public class GroupsController {

        @FXML private ListView<String> groupsList;
        @FXML private Label groupNameLabel;
        @FXML private Label groupDescLabel;
        @FXML private ListView<String> membersList;

        private final ObjectMapper mapper = new ObjectMapper();
        private Long selectedGroupId = null;
        private Map<String, Long> memberMap = new HashMap<>();

        @FXML
        public void initialize() {
            loadGroups();
            groupsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String[] parts = newVal.split(" - ");
                    selectedGroupId = Long.parseLong(parts[0]);
                    loadGroupDetails(selectedGroupId);
                    loadMembers(selectedGroupId);
                }
            });
        }

        private void loadGroups() {
            try {
                var response = ApiClient.get("/groups");
                if (response.statusCode() == 200) {
                    groupsList.getItems().clear();
                    JsonNode arr = mapper.readTree(response.body());
                    for (JsonNode g : arr) {
                        Long id = g.get("id").asLong();
                        String name = g.get("name").asText();
                        groupsList.getItems().add(id + " - " + name);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadGroupDetails(Long groupId) {
            try {
                var response = ApiClient.get("/groups/" + groupId);
                if (response.statusCode() == 200) {
                    JsonNode g = mapper.readTree(response.body());
                    groupNameLabel.setText("Názov: " + g.get("name").asText());
                    groupDescLabel.setText("Popis: " + g.get("description").asText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadMembers(Long groupId) {
            try {
                var response = ApiClient.get("/memberships/group/" + groupId);
                if (response.statusCode() == 200) {
                    membersList.getItems().clear();
                    memberMap.clear();
                    JsonNode arr = mapper.readTree(response.body());
                    for (JsonNode m : arr) {
                        String userName = m.get("user").get("name").asText();
                        String role = m.get("role").asText();
                        Long membershipId = m.get("membershipId").asLong();
                        String display = userName + " (" + role + ")";
                        membersList.getItems().add(display);
                        memberMap.put(display, membershipId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void openCreateGroupDialog() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/create_group.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Create Group");
                stage.showAndWait();
                loadGroups();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void handleAddMember() {
            if (selectedGroupId == null) return;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_member.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));

                AddMemberController controller = loader.getController();
                controller.setGroupId(selectedGroupId);

                stage.setTitle("Add Member");
                stage.showAndWait();


                loadMembers(selectedGroupId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void handleRemoveMember() {
            String selected = membersList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Long membershipId = memberMap.get(selected);
            try {
                var response = ApiClient.delete("/memberships/" + membershipId);
                if (response.statusCode() == 200) {
                    loadMembers(selectedGroupId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @FXML
        private void handleDeleteGroup() {
            if (selectedGroupId == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Naozaj chceš zmazať skupinu?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        var response = ApiClient.delete("/groups/" + selectedGroupId);
                        if (response.statusCode() == 200) {
                            loadGroups();
                            membersList.getItems().clear();
                            groupNameLabel.setText("");
                            groupDescLabel.setText("");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
