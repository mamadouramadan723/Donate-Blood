package com.rmd.giveblood.notification;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.rmd.giveblood.R;
import com.rmd.giveblood.notification.activity_fragment.Notification_Activity;

//pour s'auto envoyer de notifications

public class Notification_AutoSendNotif_OreoAndAbove extends ContextWrapper {
    private final NotificationManager notifManager;

    private static final String CHANNEL_HIGH_ID = "com.rmd.android.e_mou9af_v4.ui.home.HIGH_CHANNEL";
    private static final String CHANNEL_HIGH_NAME = "High Channel";

    private static final String CHANNEL_DEFAULT_ID = "com.rmd.android.e_mou9af_v4.ui.home.DEFAULT_CHANNEL";
    private static final String CHANNEL_DEFAUL_NAME = "Default Channel";


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification_AutoSendNotif_OreoAndAbove(Context base ) {
        super( base );

        notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long [] swPattern = new long[] { 0, 500, 110, 500, 110, 450, 110, 200, 110,
                170, 40, 450, 110, 200, 110, 170, 40, 500 };

        NotificationChannel notificationChannelHigh = new NotificationChannel(
                CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, notifManager.IMPORTANCE_HIGH );
        notificationChannelHigh.enableLights( true );
        notificationChannelHigh.setLightColor( Color.RED );
        notificationChannelHigh.setShowBadge( true );
        notificationChannelHigh.enableVibration( true );
        notificationChannelHigh.setVibrationPattern( swPattern );
        notificationChannelHigh.setLockscreenVisibility( Notification.VISIBILITY_PUBLIC );
        notifManager.createNotificationChannel( notificationChannelHigh );

        NotificationChannel notificationChannelDefault = new NotificationChannel(
                CHANNEL_DEFAULT_ID, CHANNEL_DEFAUL_NAME, notifManager.IMPORTANCE_DEFAULT );
        notificationChannelDefault.enableLights( true );
        notificationChannelDefault.setLightColor( Color.WHITE );
        notificationChannelDefault.enableVibration( true );
        notificationChannelDefault.setShowBadge( false );
        notifManager.createNotificationChannel( notificationChannelDefault );
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notify(int id, boolean prioritary, String title, String message ) {
        String channelId = prioritary ? CHANNEL_HIGH_ID : CHANNEL_DEFAULT_ID;
        Resources res = getApplicationContext().getResources();
        Context context = getApplicationContext();
        long [] swPattern = new long[] { 0, 500, 110, 500, 110, 450, 110, 200, 110,
                170, 40, 450, 110, 200, 110, 170, 40, 500 };

        /* Lien avec l'activité à ouvrir :  */
        Intent notificationIntent = new Intent(context, Notification_Activity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 456, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder( getApplicationContext(), channelId )
                .setContentIntent(contentIntent) // On injecte le contentIntent
                .setContentTitle( title )
                .setContentText( message )
                .setSmallIcon( R.drawable.applogo )
                .setLargeIcon( BitmapFactory.decodeResource(res, R.drawable.applogo) )
                .setAutoCancel( true )
                .setVibrate(swPattern)
                .build();
        notifManager.notify( id, notification );
    }
}