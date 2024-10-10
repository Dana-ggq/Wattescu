package com.example.wattescu.entities;

public class User {

    private String id;
    private String email;
    private String username;
    private boolean isVerified;

    public User() {
    }

    public User(String email, String username, boolean isVerified) {
        this.email = email;
        this.username = username;
        this.isVerified = isVerified;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
