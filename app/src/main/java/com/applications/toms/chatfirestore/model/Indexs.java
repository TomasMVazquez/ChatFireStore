package com.applications.toms.chatfirestore.model;

public class Indexs {

    private String idChat;
    private String sentedTo;
    private Integer index;

    public Indexs() {
    }

    public Indexs(String idChat, String sentedTo) {
        this.idChat = idChat;
        this.sentedTo = sentedTo;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idIndex) {
        this.idChat = idIndex;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getSentedTo() {
        return sentedTo;
    }

    public void setSentedTo(String sentedTo) {
        this.sentedTo = sentedTo;
    }
}
