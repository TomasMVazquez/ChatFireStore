package com.applications.toms.chatfirestore.model;

public class Chat {

    private Integer id;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;

    public Chat(Integer id,String sender, String receiver, String message, boolean isseen) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
    }

    public Chat() {
    }

    public Integer getId() {
        return id;
    }

    public boolean isIsseen() {
        return isseen;
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
