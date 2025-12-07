package com.example.controller;

import com.example.api.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserController {

    @FXML private Label userNameLabel;
    @FXML private Label userIdLabel;
    @FXML private Label userEmailLabel;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordErrorLabel;

    @FXML private TextField newEmailField;
    @FXML private TextField confirmEmailField;
    @FXML private Label emailErrorLabel;

    private String currentUserName;
    private Long currentUserId;
    private String currentEmail;

    private final ObjectMapper mapper = new ObjectMapper();

    public void setUserData(String name, Long id, String email) {
        this.currentUserName = name;
        this.currentUserId = id;
        this.currentEmail = email;

        userNameLabel.setText("Meno: " + name);
        userIdLabel.setText("ID: " + id);
        userEmailLabel.setText("Email: " + email);
    }

    @FXML
    private void handlePasswordChange() {
        String pass1 = newPasswordField.getText();
        String pass2 = confirmPasswordField.getText();

        if (pass1.isEmpty() || pass2.isEmpty()) {
            passwordErrorLabel.setText("Heslo nesmie byť prázdne!");
            passwordErrorLabel.setVisible(true);
            return;
        }
        if (pass1.length() < 6) {
            passwordErrorLabel.setText("Heslo musí mať aspoň 6 znakov!");
            passwordErrorLabel.setVisible(true);
            return;
        }
        if (!pass1.equals(pass2)) {
            passwordErrorLabel.setText("Heslá sa nezhodujú!");
            passwordErrorLabel.setVisible(true);
            return;
        }

        try {
            var body = new java.util.HashMap<String, String>();
            body.put("newPassword", pass1);
            var response = ApiClient.post("/users/" + currentUserId + "/change-password", body);
            if (response.statusCode() == 200) {
                passwordErrorLabel.setVisible(false);
                System.out.println("Heslo úspešne zmenené.");
            } else {
                JsonNode json = mapper.readTree(response.body());
                passwordErrorLabel.setText("Chyba: " + json.get("message").asText());
                passwordErrorLabel.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            passwordErrorLabel.setText("Server error.");
            passwordErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleEmailChange() {
        String email1 = newEmailField.getText();
        String email2 = confirmEmailField.getText();

        if (email1.isEmpty() || email2.isEmpty()) {
            emailErrorLabel.setText("Email nesmie byť prázdny!");
            emailErrorLabel.setVisible(true);
            return;
        }
        if (!email1.equals(email2)) {
            emailErrorLabel.setText("Emaily sa nezhodujú!");
            emailErrorLabel.setVisible(true);
            return;
        }

        try {
            var body = new java.util.HashMap<String, String>();
            body.put("newEmail", email1);
            var response = ApiClient.post("/users/" + currentUserId + "/change-email", body);
            if (response.statusCode() == 200) {
                currentEmail = email1;
                userEmailLabel.setText("Email: " + currentEmail);
                emailErrorLabel.setVisible(false);
                System.out.println("Email úspešne zmenený.");
            } else {
                JsonNode json = mapper.readTree(response.body());
                emailErrorLabel.setText("Chyba: " + json.get("message").asText());
                emailErrorLabel.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            emailErrorLabel.setText("Server error.");
            emailErrorLabel.setVisible(true);
        }
    }
}
