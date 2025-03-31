package hu.ujvari.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userId;
    private String username;
    private String fullName;
    private List<String> roles;
    private Map<String, Boolean> permissions;
    
    public User() {
        this.roles = new ArrayList<>();
        this.permissions = new HashMap<>();
    }
    
    // Segédmetódusok a szerepkörök ellenőrzéséhez
    public boolean isDoctor() {
        return roles.contains("DOCTOR");
    }
    
    public boolean isNurse() {
        return roles.contains("NURSE");
    }
    
    public boolean isAssistant() {
        return roles.contains("ASSISTANT");
    }
    
    // Jogosultság ellenőrzés
    public boolean hasPermission(String permission) {
        return permissions.getOrDefault(permission, false);
    }
    
    // Getterek
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        // Védett másolatot adunk vissza, hogy a belső lista ne módosuljon kívülről
        return new ArrayList<>(roles);
    }

    public Map<String, Boolean> getPermissions() {
        // Védett másolatot adunk vissza, hogy a belső Map ne módosuljon kívülről
        return new HashMap<>(permissions);
    }

    // Setterek
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRoles(List<String> roles) {
        this.roles = new ArrayList<>(roles); // Védett másolatot hozunk létre
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = new HashMap<>(permissions); // Védett másolatot hozunk létre
    }
    
    @Override
    public String toString() {
        return "Felhasználó: " + fullName + " (" + username + "), szerepkörök: " + roles;
    }
}