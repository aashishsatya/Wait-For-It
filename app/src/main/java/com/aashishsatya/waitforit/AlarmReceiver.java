package com.aashishsatya.waitforit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// almost copied from http://javatechig.com/android/repeat-alarm-example-in-android
// also refer http://www.101apps.co.za/articles/scheduling-android-s-repeating-alarms.html

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // lots of things to be done

        // get the old details and the station name

        // get the new details

        // update the single alarm

        // delete the old file

        // write the new file

        // but for now let's just show a toast

        Log.d("AlarmFlag>", "Alarm was here");

        Intent service1 = new Intent(context, MyAlarmService.class);
        context.startService(service1);
        context.stopService(service1);

        Toast.makeText(context, "Alarm sounded, finished services", Toast.LENGTH_SHORT).show();

    }
}
