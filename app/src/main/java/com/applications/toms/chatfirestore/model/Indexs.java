package com.applications.toms.chatfirestore.model;

public class Indexs {

    private String idIndex;
    private Integer index;

    public Indexs() {
    }

    public Indexs(String idIndex, Integer index) {
        this.idIndex = idIndex;
        this.index = index;
    }

    public String getIdIndex() {
        return idIndex;
    }

    public void setIdIndex(String idIndex) {
        this.idIndex = idIndex;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
