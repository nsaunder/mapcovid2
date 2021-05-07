package com.example.mapcovid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

public class AlarmHelper {
    private Context mContext;
    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private Constant constants;

    AlarmHelper(Context context) {
        mContext = context;
        constants = new Constant();
    }

    void createNotification()
    {
        Intent intent = new Intent(mContext , HomeActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.notification_logo);

        //send specific notification if we have current location
        if(constants.getCurrentLocation() != null) {
            //retrieves city that user is located at atm
            City current = constants.get_city(constants.getCurrentLocation());
            //notification text
            String content_title = "You're Currently at " + constants.getCurrentLocation();
            String msg = current.city_notification_message();
            String title_and_msg = content_title + "\n" + msg;

            mBuilder.setContentTitle("Good Morning!")
                    .setContentText(content_title)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(title_and_msg))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);
        }
        else {
            //we don't have current location -> send generic notification
            mBuilder.setContentTitle("Good Morning!")
                    .setContentText("Stay Safe and Mask Up!")
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(resultPendingIntent);
        }


        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }
}
}
