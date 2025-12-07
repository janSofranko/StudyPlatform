package com.example.controller;

import com.example.api.ApiClient;
import com.example.notify.NotificationClient;
import com.example.notify.NotificationStore;
import com.example.store.CurrentUserStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class AppController {

    @FXML private AnchorPane contentPane;
    @FXML private javafx.scene.control.Label welcomeLabel;
    @FXML private Button notificationsButton;

    private String currentUserName;
    private Long currentUserId;
    private String currentEmail;

    private NotificationClient notificationClient;
    private Thread colorWatcher;


    public void initUser(String username, Long id, String email) {
        this.currentUserName = username;
        this.currentUserId = id;
        this.currentEmail = email;

        CurrentUserStore.set(id, username, email);
        welcomeLabel.setText("Vitaj, " + username);

        notificationClient = new NotificationClient(currentUserId);
        notificationClient.start();

        checkUnreadNotifications();
        startButtonColorWatcher();
        openHome();
    }


    private void checkUnreadNotifications() {
        try {
            var resp = ApiClient.get("/notifications/" + currentUserId + "/unread-count");
            if (resp.statusCode() == 200) {
                long unread = new ObjectMapper().readTree(resp.body()).asLong();
                if (unread > 0) NotificationStore.setUnread(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startButtonColorWatcher() {
        if (colorWatcher != null && colorWatcher.isAlive()) return;

        colorWatcher = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try { Thread.sleep(800); }
                catch (InterruptedException e) { break; }

                boolean unread = NotificationStore.hasUnread();
                Platform.runLater(() ->
                        notificationsButton.setStyle(unread
                                ? "-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;"
                                : "-fx-background-color: #0598ff; -fx-text-fill: white; -fx-font-weight: bold;")
                );
            }
        });

        colorWatcher.setDaemon(true);
        colorWatcher.start();
    }

    private void loadContent(String fxmlFile) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentPane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void openGroups() { loadContent("/groups.fxml"); }
    @FXML private void openTasks() { loadContent("/tasks.fxml"); }
    @FXML private void openResources() { loadContent("/resources.fxml"); }

    @FXML
    public void openHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            AnchorPane pane = loader.load();

            HomeController homeController = loader.getController();

            homeController.setUserContext(currentUserName);

            contentPane.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openStats() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/stats.fxml"));
            AnchorPane pane = loader.load();
            URL cssUrl = getClass().getResource("/style.css");
            if (cssUrl != null) {
                pane.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("⚠️ CSS súbor styles.css sa nenašiel!");
            }

            contentPane.getChildren().setAll(pane);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void openUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user.fxml"));
            AnchorPane pane = loader.load();

            UserController userController = loader.getController();
            userController.setUserData(currentUserName, currentUserId, currentEmail);

            contentPane.getChildren().setAll(pane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openNotifications() {
        NotificationStore.markRead();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/notifications.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            com.example.controller.NotificationsController controller = loader.getController();
            controller.setUserId(currentUserId);

            stage.setTitle("Notifikácie");
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (notificationClient != null) notificationClient.stop();
            if (colorWatcher != null) colorWatcher.interrupt();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            AnchorPane root = loader.load();
            contentPane.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
