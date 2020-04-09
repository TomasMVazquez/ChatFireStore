package com.applications.toms.chatfirestore.model;

public class Message {

    private Integer id;
    private boolean isseen;
    private String message;
    private String receiver;
    private String sender;

    public Message() {
    }

    public Message(Integer id, boolean isseen, String message, String receiver, String sender) {
        this.id = id;
        this.isseen = isseen;
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
