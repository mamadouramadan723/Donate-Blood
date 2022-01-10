package com.rmd.donateblood.notification;


import com.rmd.donateblood.notification.models.Response;
import com.rmd.donateblood.notification.models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization: key=AAAA-bkuKEU:APA91bHHtsL29sinNPUYuwZDnhfaVaVA5kVU5j8GyndK9ujvoML6uOBN4J7YdAybwlE2NFnKHKqA7DVIIOXs6dPNb2Ttf5vgNvWjSKlnwPEFjYDl8HEo2ox3RE0nXNH85LTtBjjNel4I"
            //cette clé c'est celle du serveur dans les identidiants cloud messenging dans les paramètres du projet
    })
    @POST("fcm/send")
    Call <Response> sendNotifiaction(@Body Sender body);
}
