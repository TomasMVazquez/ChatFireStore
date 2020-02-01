package com.applications.toms.chatfirestore.model;

public class Chat {

    private Integer id;
    private String sender;
    private String receiver;
    private String message;

    public Chat(Integer id,String sender, String receiver, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public Chat() {
    }

    public Integer getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }
}
