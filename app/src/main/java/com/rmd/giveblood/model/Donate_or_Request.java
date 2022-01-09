package com.rmd.giveblood.model;

public class Donate_or_Request {
    private String userId, donate_request_id, nom, phone_number, mail, image_url, blood_group, city, description, time, type;

    public Donate_or_Request() {
    }

    public Donate_or_Request(String userId, String donate_request_id, String nom, String phone_number,
                             String mail, String image_url, String blood_group, String city,
                             String description, String time, String type) {
        this.userId = userId;
        this.donate_request_id = donate_request_id;
        this.nom = nom;
        this.phone_number = phone_number;
        this.mail = mail;
        this.image_url = image_url;
        this.blood_group = blood_group;
        this.city = city;
        this.description = description;
        this.time = time;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDonate_request_id() {
        return donate_request_id;
    }

    public void setDonate_request_id(String donate_request_id) {
        this.donate_request_id = donate_request_id;
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

    public String getBlood_group() {
        return blood_group;
    }

    public void setBlood_group(String blood_group) {
        this.blood_group = blood_group;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
