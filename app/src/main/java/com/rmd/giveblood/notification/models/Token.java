package com.rmd.giveblood.notification.models;

public class Token {
    /*An ID issued by the GCM connection servers to the client app that allows it to receive messages.
    Note that registration tokens must be kept secret.
     */
    private String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

