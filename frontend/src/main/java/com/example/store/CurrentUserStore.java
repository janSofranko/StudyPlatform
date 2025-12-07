package com.example.store;

public class CurrentUserStore {
    private static Long userId;
    private static String userName;
    private static String userEmail;

    public static void set(Long id, String name, String email) {
        userId = id;
        userName = name;
        userEmail = email;
    }

    public static Long getUserId() { return userId; }
    public static String getUserName() { return userName; }
    public static String getUserEmail() { return userEmail; }
}
