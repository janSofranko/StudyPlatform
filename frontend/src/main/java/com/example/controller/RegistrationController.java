package com.example.controller;

import com.example.api.ApiClient;
import com.example.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class RegistrationController {

    @FXML private TextField Username1;
    @FXML private TextField EmailField;
    @FXML private PasswordField Password1;
    @FXML private PasswordField Password;
    @FXML private Label invalidRegistration;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private void handleRegister() {
        try {
            // ---- FRONTEND VALIDÁCIA ----
            if (Username1.getText().isEmpty() ||
                    EmailField.getText().isEmpty() ||
                    Password1.getText().isEmpty() ||
                    Password.getText().isEmpty()) {

                showError("All fields required!");
                return;
            }

            if (!Password1.getText().equals(Password.getText())) {
                showError("Passwords do not match!");
                return;
            }

            // ---- PRÍPRAVA REQUESTU ----
            var request = new RegisterRequest(
                    Username1.getText(),
                    EmailField.getText(),
                    Password1.getText()
            );

            var response = ApiClient.post("/users/register", request);

            // ---- ÚSPEŠNÁ REGISTRÁCIA ----
            if (response.statusCode() == 200) {
                invalidRegistration.setVisible(false);
                goToLogin();
                return;
            }

            // ---- BACKEND VALIDÁCIA / CHYBA ----
            String prettyMessage = parseErrorMessage(response.body());
            showError(prettyMessage);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Server connection error.");
        }
    }

    // === Zobrazenie chyby v UI ===
    private void showError(String msg) {
        invalidRegistration.setText(msg);
        invalidRegistration.setVisible(true);
    }

    // === Parsovanie JSON chyby z backendu ===
    private String parseErrorMessage(String body) {
        try {
            JsonNode json = mapper.readTree(body);

            if (json.has("errors")) {
                JsonNode errors = json.get("errors");

                String field = errors.fieldNames().next();
                return errors.get(field).asText();
            }

            if (json.has("message")) {
                return json.get("message").asText();
            }

        } catch (Exception ignored) {}

        return "Registrácia zlyhala.";
    }

    // === Návrat na login okno ===

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // zober aktuálne okno (Stage) z ľubovoľného prvku
            Stage stage = (Stage) Username1.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
