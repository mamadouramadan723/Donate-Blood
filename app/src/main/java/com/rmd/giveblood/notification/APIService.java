package com.rmd.giveblood.notification;


import com.rmd.giveblood.notification.models.Response;
import com.rmd.giveblood.notification.models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization: key=AAAA-LUwYwU:APA91bE-mu7weQJs68RVsHPhWFQHNRan3rAv2GqnDsiHQomhINjKzgVT3a6QR07pR3RzXKRK63RdXO1Pp26AVChSa1fwMuhUuTuHZOvOxKjhxDqHOFxWfZ_QnMljLyj2zfCYObz53VcA"
            //cette clé c'est celle du serveur dans les identidiants cloud messenging dans les paramètres du projet
    })
    @POST("fcm/send")
    Call <Response> sendNotifiaction(@Body Sender body);
}
