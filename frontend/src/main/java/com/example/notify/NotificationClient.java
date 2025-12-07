package com.example.notify;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Executors;


public class NotificationClient {

    private final long userId;
    private volatile boolean running = false;

    public NotificationClient(long userId) {
        this.userId = userId;
    }

    public void start() {
        if (running) return;
        running = true;

        Executors.newSingleThreadExecutor().submit(() -> {
            while (running) {
                try {
                    var url = "http://localhost:8080/api/notifications/stream/" + userId;
                    var conn = new URL(url).openConnection();
                    conn.setRequestProperty("Accept", "text/event-stream");
                    conn.setReadTimeout(0);

                    try (var in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        String lastEvent = null;

                        while (running && (line = in.readLine()) != null) {
                            if (line.startsWith("event:")) {
                                lastEvent = line.substring(6).trim();
                            } else if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if ("notification".equals(lastEvent)) {
                                    Platform.runLater(() -> NotificationStore.add(data));
                                }
                            }

                        }
                    }


                    Thread.sleep(1000);
                } catch (Exception e) {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    public void stop() {
        running = false;
    }
}
