package com.LightMediaApps.TimeCapsules.model;

public class User {

    private String username;

    private String image;

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public User() {

    }

    public User(String username, String image) {
        this.username = username;
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public String getImage() {
        return image;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
