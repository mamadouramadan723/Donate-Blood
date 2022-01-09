package com.rmd.giveblood.model;

public class Donate_or_Request_Id_Type {
    String donate_request_id, type;

    public Donate_or_Request_Id_Type() {
    }

    public Donate_or_Request_Id_Type(String donate_request_id, String type) {
        this.donate_request_id = donate_request_id;
        this.type = type;
    }

    public String getDonate_request_id() {
        return donate_request_id;
    }

    public void setDonate_request_id(String donate_request_id) {
        this.donate_request_id = donate_request_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
