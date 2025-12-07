    package com.example.notify;

    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;

    public class NotificationStore {
        private static final ObservableList<String> messages = FXCollections.observableArrayList();
        private static volatile boolean hasUnread = false;

        public static void add(String msg) {
            messages.add(0, msg);
            hasUnread = true;
        }

        public static ObservableList<String> getMessages() {
            return messages;
        }

        public static boolean hasUnread() {
            return hasUnread;
        }

        public static void markRead() {
            hasUnread = false;
        }


        public static void setUnread(boolean value) {
            hasUnread = value;
        }
    }
