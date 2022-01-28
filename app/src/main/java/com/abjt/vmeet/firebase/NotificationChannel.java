package com.abjt.vmeet.firebase;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.abjt.vmeet.utils.Constants;

public class NotificationChannel extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // NOTIFICATIONS CHANNEL
            createPushNotificationChannel();
        }

    }

    private void createPushNotificationChannel() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        android.app.NotificationChannel notificationChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "VMeet Notification";
            notificationChannel = new android.app.NotificationChannel(Constants.PUSH_NOTIFICATION_CHANNEL_ID, channelName,
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);


            notificationManager.createNotificationChannel(notificationChannel);


        }
    }

}
