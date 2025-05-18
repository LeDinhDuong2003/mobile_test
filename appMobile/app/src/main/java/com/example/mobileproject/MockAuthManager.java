// app/src/main/java/com/example/mobileproject/MockAuthManager.java
package com.example.mobileproject;

import com.example.mobileproject.model.User;

public class MockAuthManager {
    private static MockAuthManager instance;
    private Integer currentUserId;
    private User currentUser;

    private MockAuthManager() {
        setMockUser(2, "John Doe", "Student");
    }

    public static MockAuthManager getInstance() {
        if (instance == null) {
            instance = new MockAuthManager();
        }
        return instance;
    }

    public void setMockUser(int userId, String fullName, String role) {
        this.currentUserId = userId;
        this.currentUser = new User();
        this.currentUser.setUserId(userId);
        this.currentUser.setFullName(fullName);
        this.currentUser.setRole(role);
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}