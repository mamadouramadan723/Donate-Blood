package com.rmd.giveblood.model;

public class User {
    private String  userId, nom, phone_number, mail, image_url;

    public User() {
    }

    public User(String userId, String nom, String phone_number, String mail, String image_url) {
        this.userId = userId;
        this.nom = nom;
        this.phone_number = phone_number;
        this.mail = mail;
        this.image_url = image_url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
