package com.rmd.donateblood.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rmd.donateblood.notification.activity_fragment.Notification_Activity;

public class Firebase_Messaging extends FirebaseMessagingService {

    private CollectionReference token_ref;

    @Override
    public void onNewToken(@NonNull String token) {

        token_ref = FirebaseFirestore.getInstance().collection("tokens");
        token_ref.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(token)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("token", "Success");
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //getcurrent current user from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sharedPreferences.getString("Current_USERID", "None");

        String sent = "" + remoteMessage.getData().get("sent");
        String user = "" + remoteMessage.getData().get("user");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            if (!savedCurrentUser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoAndAboveNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = "" + remoteMessage.getData().get("user");
        String icon = "" + remoteMessage.getData().get("icon");
        String body = "" + remoteMessage.getData().get("body");
        String title = "" + remoteMessage.getData().get("title");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        //Intent intent = new Intent(this, Notification_Activity.class);
        Intent intent = new Intent(this, Notification_Activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent) // On injecte le contentIntent
                .setContentTitle(title)
                .setSound(defSoundUri)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setVibrate(new long[]{0, 500, 110, 500, 110, 450, 110, 200, 110,
                        170, 40, 450, 110, 200, 110, 170, 40, 500})
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoAndAboveNotification(RemoteMessage remoteMessage) {
        String user = "" + remoteMessage.getData().get("user");
        String icon = "" + remoteMessage.getData().get("icon");
        String body = "" + remoteMessage.getData().get("body");
        String title = "" + remoteMessage.getData().get("title");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        //Intent intent = new Intent(this, Notification_Activity.class);
        Intent intent = new Intent(this, Notification_Activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification_OreoAndAbove notification1 = new Notification_OreoAndAbove(this);
        Notification.Builder builder = notification1.getONotifications(false, title, body, pendingIntent, defSoundUri, icon);

        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getNotificationManager().notify(j, builder.build());
    }
}
