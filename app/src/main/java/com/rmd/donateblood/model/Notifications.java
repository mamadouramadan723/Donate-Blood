package com.rmd.donateblood.model;

public class Notifications {
    private String userId, image_url, notifId, notif_time, notif_desc, type_notif, donate_request_id;

    //userId = id de celui qui est à l'origine de l'offre, de la demande, image_url sa photo de profile
    public Notifications() {
    }

    public Notifications(String userId, String image_url, String notifId, String notif_time,
                         String notif_desc, String type_notif, String donate_request_id) {
        this.userId = userId;
        this.image_url = image_url;
        this.notifId = notifId;
        this.notif_time = notif_time;
        this.notif_desc = notif_desc;
        this.type_notif = type_notif;
        this.donate_request_id = donate_request_id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
        this.notifId = notifId;
    }

    public String getNotif_time() {
        return notif_time;
    }

    public void setNotif_time(String notif_time) {
        this.notif_time = notif_time;
    }

    public String getNotif_desc() {
        return notif_desc;
    }

    public void setNotif_desc(String notif_desc) {
        this.notif_desc = notif_desc;
    }

    public String getType_notif() {
        return type_notif;
    }

    public void setType_notif(String type_notif) {
        this.type_notif = type_notif;
    }

    public String getDonate_request_id() {
        return donate_request_id;
    }

    public void setDonate_request_id(String donate_request_id) {
        this.donate_request_id = donate_request_id;
    }
}
