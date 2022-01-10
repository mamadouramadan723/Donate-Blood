package com.rmd.donateblood.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.rmd.donateblood.R;

public class Notification_OreoAndAbove extends ContextWrapper {

    private static final String CHANNEL_HIGH_ID = "com.rmd.android.e_mou9af_v4.HIGH_CHANNEL";
    private static final String CHANNEL_HIGH_NAME = "High Channel";

    private static final String CHANNEL_DEFAULT_ID = "com.rmd.android.e_mou9af_v4.DEFAULT_CHANNEL";
    private static final String CHANNEL_DEFAUL_NAME = "Default Channel";
    private NotificationManager notificationManager;

    public Notification_OreoAndAbove(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //O = 26
            /*L'API 26 d'Android change la manière de produire les notifications.
            Le principal changement réside dans l'obligation d'utiliser les NotificationChannel.
            Un NotificationChannel, est en quelle que sorte, une pré-configuration pour un ensemble de notifications.
            Lors de sa création, une notification est associée à un channel */
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        long [] swPattern = new long[] { 0, 500, 110, 500, 110, 450, 110, 200, 110,
                170, 40, 450, 110, 200, 110, 170, 40, 500 };

        NotificationChannel notificationChannelHigh = new NotificationChannel(
                CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannelHigh.enableLights(true);
        notificationChannelHigh.setLightColor(Color.RED);
        notificationChannelHigh.setShowBadge(true);
        notificationChannelHigh.enableVibration(true);
        notificationChannelHigh.setVibrationPattern(swPattern);
        notificationChannelHigh.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(notificationChannelHigh);

        NotificationChannel notificationChannelDefault = new NotificationChannel(
                CHANNEL_DEFAULT_ID, CHANNEL_DEFAUL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannelDefault.enableLights(true);
        notificationChannelDefault.setLightColor(Color.WHITE);
        notificationChannelDefault.enableVibration(true);
        notificationChannelDefault.setShowBadge(false);
        notificationChannelDefault.setVibrationPattern(swPattern);
        getNotificationManager().createNotificationChannel(notificationChannelDefault);
    }

    public NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getONotifications(boolean prioritary, String title, String message,
                                                  PendingIntent pendingIntent, Uri soundUri, String icon) {
        String channelId = prioritary ? CHANNEL_HIGH_ID : CHANNEL_DEFAULT_ID;

        return new Notification.Builder(getApplicationContext(), channelId)
                .setContentIntent(pendingIntent) // On injecte le contentIntent
                .setContentTitle(title)
                .setSound(soundUri)
                .setContentText(message)
                .setSmallIcon(R.drawable.applogo)
                .setLargeIcon(Icon.createWithContentUri(icon))
                .setAutoCancel(true);
    }

}