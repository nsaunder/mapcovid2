package com.example.mapcovid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmHelper alarmHelper = new AlarmHelper(context);
        alarmHelper.createNotification();

    }
}
