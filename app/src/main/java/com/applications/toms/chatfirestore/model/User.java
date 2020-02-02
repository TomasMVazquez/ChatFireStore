package com.applications.toms.chatfirestore.model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;

    public User(String id, String username, String imageURL,String status) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getStatus() {
        return status;
    }
}
