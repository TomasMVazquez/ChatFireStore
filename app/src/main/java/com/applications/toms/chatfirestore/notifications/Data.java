package com.applications.toms.chatfirestore.notifications;

public class Data {

    private String user;
    private int icon;
    private String title;
    private String body;
    private String sentedTo;

    public Data(String user, int icon, String title, String body, String sentedTo) {
        this.user = user;
        this.icon = icon;
        this.title = title;
        this.body = body;
        this.sentedTo = sentedTo;
    }

    public Data() {
    }

    public String getUser() {
        return user;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getSentedTo() {
        return sentedTo;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSentedTo(String sentedTo) {
        this.sentedTo = sentedTo;
    }
}
