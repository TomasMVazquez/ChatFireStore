package com.applications.toms.chatfirestore.model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String search;
    private String token;

    public User(String id, String username, String imageURL,String status,String search,String token) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.token = token;
    }

    public User() {
    }

    public String getSearch() {
        return search;
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

    public String getToken() {
        return token;
    }
}
